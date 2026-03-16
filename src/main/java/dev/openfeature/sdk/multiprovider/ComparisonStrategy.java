package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Getter;

/**
 * Comparison strategy.
 *
 * <p>Evaluates all providers in parallel and compares successful results.
 * If all providers agree on the value, the fallback provider's result is returned.
 * If providers disagree, the optional {@code onMismatch} callback is invoked
 * and the fallback provider's result is returned.
 * If any provider returns an error, all errors are collected and a
 * {@link ErrorCode#GENERAL} error is returned.
 */
public class ComparisonStrategy implements Strategy {

    private static final long DEFAULT_TIMEOUT_MS = 30_000;

    @Getter
    private final String fallbackProvider;

    private final BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch;
    private final ExecutorService executorService;
    private final boolean ownsExecutorService;
    private final long timeoutMs;

    /**
     * Constructs a comparison strategy with a fallback provider.
     *
     * <p>Uses a shared {@link ForkJoinPool#commonPool()} for parallel evaluation.
     *
     * @param fallbackProvider provider name to use as fallback when successful
     *                         providers disagree
     */
    public ComparisonStrategy(String fallbackProvider) {
        this(fallbackProvider, null);
    }

    /**
     * Constructs a comparison strategy with fallback provider and mismatch callback.
     *
     * <p>Uses a shared {@link ForkJoinPool#commonPool()} for parallel evaluation.
     *
     * @param fallbackProvider provider name to use as fallback when successful
     *                         providers disagree
     * @param onMismatch       callback invoked with all successful evaluations
     *                         when they disagree
     */
    public ComparisonStrategy(
            String fallbackProvider,
            BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch) {
        this(fallbackProvider, onMismatch, ForkJoinPool.commonPool(),
                false, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Constructs a comparison strategy with a caller-supplied executor.
     *
     * @param fallbackProvider provider name to use as fallback when successful
     *                         providers disagree
     * @param onMismatch       callback invoked with all successful evaluations
     *                         when they disagree (may be {@code null})
     * @param executorService  executor to use for parallel evaluation
     * @param timeoutMs        maximum time in milliseconds to wait for all
     *                         providers to complete
     */
    public ComparisonStrategy(
            String fallbackProvider,
            BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch,
            ExecutorService executorService,
            long timeoutMs) {
        this(fallbackProvider, onMismatch, executorService,
                false, timeoutMs);
    }

    private ComparisonStrategy(
            String fallbackProvider,
            BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch,
            ExecutorService executorService,
            boolean ownsExecutorService,
            long timeoutMs) {
        this.fallbackProvider = Objects.requireNonNull(
                fallbackProvider, "fallbackProvider must not be null");
        this.onMismatch = onMismatch;
        this.executorService = Objects.requireNonNull(
                executorService, "executorService must not be null");
        this.ownsExecutorService = ownsExecutorService;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public <T> ProviderEvaluation<T> evaluate(
            Map<String, FeatureProvider> providers,
            String key,
            T defaultValue,
            EvaluationContext ctx,
            Function<FeatureProvider, ProviderEvaluation<T>> providerFunction) {
        if (providers.isEmpty()) {
            return ProviderEvaluation.<T>builder()
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage("No providers configured")
                    .build();
        }
        if (!providers.containsKey(fallbackProvider)) {
            throw new IllegalArgumentException(
                    "fallbackProvider not found in providers: "
                            + fallbackProvider);
        }

        Map<String, ProviderEvaluation<T>> successfulResults =
                new ConcurrentHashMap<>(providers.size());
        Map<String, String> providerErrors =
                new ConcurrentHashMap<>(providers.size());

        try {
            List<Callable<Void>> tasks = new ArrayList<>(providers.size());
            for (Map.Entry<String, FeatureProvider> entry
                    : providers.entrySet()) {
                String providerName = entry.getKey();
                FeatureProvider provider = entry.getValue();
                tasks.add(() -> {
                    try {
                        ProviderEvaluation<T> evaluation =
                                providerFunction.apply(provider);
                        if (evaluation == null) {
                            providerErrors.put(
                                    providerName, "null evaluation");
                        } else if (evaluation.getErrorCode() == null) {
                            successfulResults.put(
                                    providerName, evaluation);
                        } else {
                            providerErrors.put(
                                    providerName,
                                    evaluation.getErrorCode() + ": "
                                            + evaluation.getErrorMessage());
                        }
                    } catch (Exception e) {
                        providerErrors.put(
                                providerName,
                                e.getClass().getSimpleName() + ": "
                                        + e.getMessage());
                    }
                    return null;
                });
            }
            List<Future<Void>> futures =
                    executorService.invokeAll(
                            tasks, timeoutMs, TimeUnit.MILLISECONDS);
            for (Future<Void> future : futures) {
                if (future.isCancelled()) {
                    return ProviderEvaluation.<T>builder()
                            .errorCode(ErrorCode.GENERAL)
                            .errorMessage(
                                    "Comparison strategy timed out after "
                                            + timeoutMs + "ms")
                            .build();
                }
                future.get();
            }
        } catch (Exception e) {
            return ProviderEvaluation.<T>builder()
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage("Comparison strategy failed: "
                            + e.getMessage())
                    .build();
        }

        if (!providerErrors.isEmpty()) {
            return ProviderEvaluation.<T>builder()
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage("Provider errors: "
                            + buildErrorSummary(providerErrors))
                    .build();
        }

        ProviderEvaluation<T> fallbackResult =
                successfulResults.get(fallbackProvider);
        if (fallbackResult == null) {
            return ProviderEvaluation.<T>builder()
                    .errorCode(ErrorCode.GENERAL)
                    .errorMessage(
                            "Fallback provider did not return a successful "
                                    + "evaluation: " + fallbackProvider)
                    .build();
        }

        if (allEvaluationsMatch(successfulResults)) {
            return fallbackResult;
        }

        if (onMismatch != null) {
            Map<String, ProviderEvaluation<?>> mismatchPayload =
                    new LinkedHashMap<>(successfulResults);
            onMismatch.accept(
                    key, Collections.unmodifiableMap(mismatchPayload));
        }
        return fallbackResult;
    }

    private String buildErrorSummary(Map<String, String> providerErrors) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : providerErrors.entrySet()) {
            if (!first) {
                builder.append("; ");
            }
            first = false;
            builder.append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue());
        }
        return builder.toString();
    }

    private <T> boolean allEvaluationsMatch(
            Map<String, ProviderEvaluation<T>> results) {
        ProviderEvaluation<T> baseline = null;
        for (ProviderEvaluation<T> evaluation : results.values()) {
            if (baseline == null) {
                baseline = evaluation;
                continue;
            }
            if (!Objects.equals(
                    baseline.getValue(), evaluation.getValue())) {
                return false;
            }
        }
        return true;
    }
}

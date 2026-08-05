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
 * If any provider returns an error, all errors are collected and a {@link MultiProviderEvaluation}
 * with {@link ErrorCode#GENERAL} and per-provider {@link ProviderError} details is returned.
 */
public class ComparisonStrategy implements Strategy {

    private static final long DEFAULT_TIMEOUT_MS = 30_000;

    @Getter
    private final String fallbackProvider;

    private final BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch;
    private final ExecutorService executorService;
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
            String fallbackProvider, BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch) {
        this(fallbackProvider, onMismatch, ForkJoinPool.commonPool(), DEFAULT_TIMEOUT_MS);
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
        this.fallbackProvider = Objects.requireNonNull(fallbackProvider, "fallbackProvider must not be null");
        this.onMismatch = onMismatch;
        this.executorService = Objects.requireNonNull(executorService, "executorService must not be null");
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
            throw new IllegalArgumentException("fallbackProvider not found in providers: " + fallbackProvider);
        }

        int capacity = providers.size() * 4 / 3 + 1;
        Map<String, ProviderEvaluation<T>> successfulResults = new ConcurrentHashMap<>(capacity);
        Map<String, ProviderError> providerErrors = new ConcurrentHashMap<>(capacity);

        try {
            List<Callable<Void>> tasks = new ArrayList<>(providers.size());
            for (Map.Entry<String, FeatureProvider> entry : providers.entrySet()) {
                String providerName = entry.getKey();
                FeatureProvider provider = entry.getValue();
                tasks.add(() -> {
                    try {
                        ProviderEvaluation<T> evaluation = providerFunction.apply(provider);
                        if (evaluation == null) {
                            providerErrors.put(
                                    providerName,
                                    ProviderError.fromResult(providerName, ErrorCode.GENERAL, "null evaluation"));
                        } else if (evaluation.getErrorCode() == null) {
                            successfulResults.put(providerName, evaluation);
                        } else {
                            providerErrors.put(
                                    providerName,
                                    ProviderError.fromResult(
                                            providerName, evaluation.getErrorCode(), evaluation.getErrorMessage()));
                        }
                    } catch (Exception e) {
                        providerErrors.put(providerName, ProviderError.fromException(providerName, e));
                    }
                    return null;
                });
            }
            List<Future<Void>> futures = executorService.invokeAll(tasks, timeoutMs, TimeUnit.MILLISECONDS);
            for (Future<Void> future : futures) {
                if (future.isCancelled()) {
                    return errorResult(
                            "Comparison strategy timed out after " + timeoutMs + "ms", providers, providerErrors);
                }
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorResult("Comparison strategy interrupted: " + e.getMessage(), providers, providerErrors);
        } catch (Exception e) {
            return errorResult("Comparison strategy failed: " + e.getMessage(), providers, providerErrors);
        }

        if (!providerErrors.isEmpty()) {
            return errorResult("Provider errors during comparison", providers, providerErrors);
        }

        ProviderEvaluation<T> fallbackResult = successfulResults.get(fallbackProvider);
        if (fallbackResult == null) {
            return errorResult(
                    "Fallback provider did not return a successful evaluation: " + fallbackProvider,
                    providers,
                    providerErrors);
        }

        if (allEvaluationsMatch(successfulResults)) {
            return fallbackResult;
        }

        if (onMismatch != null) {
            onMismatch.accept(key, orderedResults(providers, successfulResults));
        }
        return fallbackResult;
    }

    /**
     * Builds a {@link MultiProviderEvaluation} carrying per-provider error details, ordered by the
     * provider registration order so the aggregate message is stable across runs.
     */
    private <T> ProviderEvaluation<T> errorResult(
            String baseMessage, Map<String, FeatureProvider> providers, Map<String, ProviderError> providerErrors) {
        List<ProviderError> orderedErrors = new ArrayList<>(providerErrors.size());
        for (String providerName : providers.keySet()) {
            ProviderError error = providerErrors.get(providerName);
            if (error != null) {
                orderedErrors.add(error);
            }
        }
        return MultiProviderEvaluation.<T>builder()
                .errorCode(ErrorCode.GENERAL)
                .errorMessage(ProviderError.buildAggregateMessage(baseMessage, orderedErrors))
                .providerErrors(orderedErrors)
                .build();
    }

    /** Returns the successful evaluations in provider registration order. */
    private <T> Map<String, ProviderEvaluation<?>> orderedResults(
            Map<String, FeatureProvider> providers, Map<String, ProviderEvaluation<T>> successfulResults) {
        Map<String, ProviderEvaluation<?>> ordered = new LinkedHashMap<>();
        for (String providerName : providers.keySet()) {
            ProviderEvaluation<T> evaluation = successfulResults.get(providerName);
            if (evaluation != null) {
                ordered.put(providerName, evaluation);
            }
        }
        return Collections.unmodifiableMap(ordered);
    }

    private <T> boolean allEvaluationsMatch(Map<String, ProviderEvaluation<T>> results) {
        ProviderEvaluation<T> baseline = null;
        for (ProviderEvaluation<T> evaluation : results.values()) {
            if (baseline == null) {
                baseline = evaluation;
                continue;
            }
            if (!Objects.equals(baseline.getValue(), evaluation.getValue())) {
                return false;
            }
        }
        return true;
    }
}

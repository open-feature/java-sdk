package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.internal.ConfigurableThreadFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 *
 * <p>Providers that do not respond before the internal timeout do not fail the evaluation. The
 * fallback provider's result is still returned, carrying a {@link ProviderError} for each provider
 * that timed out, so that a slow provider under comparison cannot degrade evaluations. Only a
 * timeout of the fallback provider itself produces an error result.
 */
public class ComparisonStrategy implements Strategy {

    private static final long DEFAULT_TIMEOUT_MS = 30_000;

    /**
     * Shared pool used when no executor is supplied.
     *
     * <p>Provider evaluations block, so they are kept off {@link java.util.concurrent.ForkJoinPool
     * #commonPool()} to avoid starving unrelated parallel work in the host application. Threads are
     * daemon threads, so this pool never prevents JVM shutdown.
     */
    private static final ExecutorService DEFAULT_EXECUTOR =
            Executors.newCachedThreadPool(new ConfigurableThreadFactory("openfeature-comparison-strategy", true));

    @Getter
    private final String fallbackProvider;

    private final BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch;
    private final ExecutorService executorService;
    private final long timeoutMs;

    /**
     * Constructs a comparison strategy with a fallback provider.
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
     * @param fallbackProvider provider name to use as fallback when successful
     *                         providers disagree
     * @param onMismatch       callback invoked with all successful evaluations
     *                         when they disagree
     */
    public ComparisonStrategy(
            String fallbackProvider, BiConsumer<String, Map<String, ProviderEvaluation<?>>> onMismatch) {
        this(fallbackProvider, onMismatch, DEFAULT_EXECUTOR, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Constructs a comparison strategy with a caller-supplied executor and timeout.
     *
     * <p>Intentionally not public: the executor and timeout are implementation details, and the
     * public surface is kept aligned with the js-sdk reference implementation.
     *
     * @param fallbackProvider provider name to use as fallback when successful
     *                         providers disagree
     * @param onMismatch       callback invoked with all successful evaluations
     *                         when they disagree (may be {@code null})
     * @param executorService  executor to use for parallel evaluation
     * @param timeoutMs        maximum time in milliseconds to wait for all
     *                         providers to complete
     */
    ComparisonStrategy(
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
        Map<String, ProviderError> timeoutErrors = new ConcurrentHashMap<>(capacity);

        Optional<ProviderEvaluation<T>> runFailure =
                runEvaluations(providers, providerFunction, successfulResults, providerErrors, timeoutErrors);
        if (runFailure.isPresent()) {
            return runFailure.get();
        }

        if (!providerErrors.isEmpty()) {
            return errorResult(
                    "Provider errors during comparison", orderedErrors(providers, providerErrors, timeoutErrors));
        }

        ProviderEvaluation<T> fallbackResult = successfulResults.get(fallbackProvider);
        if (fallbackResult == null) {
            return errorResult(
                    fallbackFailureMessage(timeoutErrors), orderedErrors(providers, providerErrors, timeoutErrors));
        }

        if (onMismatch != null && !allEvaluationsMatch(successfulResults)) {
            onMismatch.accept(key, orderedResults(providers, successfulResults));
        }

        if (timeoutErrors.isEmpty()) {
            return fallbackResult;
        }
        // A provider under comparison was too slow. Report it, but keep serving the fallback result.
        return withProviderErrors(fallbackResult, orderedErrors(providers, providerErrors, timeoutErrors));
    }

    private String fallbackFailureMessage(Map<String, ProviderError> timeoutErrors) {
        if (timeoutErrors.containsKey(fallbackProvider)) {
            return "Fallback provider did not respond within " + timeoutMs + "ms: " + fallbackProvider;
        }
        return "Fallback provider did not return a successful evaluation: " + fallbackProvider;
    }

    /**
     * Evaluates every provider in parallel, recording each outcome into {@code successfulResults},
     * {@code providerErrors}, or, for providers that did not finish in time, {@code timeoutErrors}.
     *
     * @return an error evaluation if the parallel run itself could not complete (interruption or
     *     executor failure), otherwise {@link Optional#empty()}
     */
    private <T> Optional<ProviderEvaluation<T>> runEvaluations(
            Map<String, FeatureProvider> providers,
            Function<FeatureProvider, ProviderEvaluation<T>> providerFunction,
            Map<String, ProviderEvaluation<T>> successfulResults,
            Map<String, ProviderError> providerErrors,
            Map<String, ProviderError> timeoutErrors) {
        try {
            List<String> providerNames = new ArrayList<>(providers.keySet());
            List<Callable<Void>> tasks = new ArrayList<>(providers.size());
            for (String providerName : providerNames) {
                FeatureProvider provider = providers.get(providerName);
                tasks.add(() -> {
                    recordEvaluation(providerName, provider, providerFunction, successfulResults, providerErrors);
                    return null;
                });
            }
            // invokeAll returns futures in task submission order, which matches providerNames.
            List<Future<Void>> futures = executorService.invokeAll(tasks, timeoutMs, TimeUnit.MILLISECONDS);
            for (int i = 0; i < futures.size(); i++) {
                Future<Void> future = futures.get(i);
                String providerName = providerNames.get(i);
                if (future.isCancelled()) {
                    timeoutErrors.put(
                            providerName,
                            ProviderError.fromResult(
                                    providerName,
                                    ErrorCode.GENERAL,
                                    "Provider did not respond within " + timeoutMs + "ms"));
                } else {
                    future.get();
                }
            }
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.of(errorResult(
                    "Comparison strategy interrupted: " + e.getMessage(),
                    orderedErrors(providers, providerErrors, timeoutErrors)));
        } catch (Exception e) {
            return Optional.of(errorResult(
                    "Comparison strategy failed: " + e.getMessage(),
                    orderedErrors(providers, providerErrors, timeoutErrors)));
        }
    }

    /** Evaluates a single provider, recording either its result or its error. */
    private <T> void recordEvaluation(
            String providerName,
            FeatureProvider provider,
            Function<FeatureProvider, ProviderEvaluation<T>> providerFunction,
            Map<String, ProviderEvaluation<T>> successfulResults,
            Map<String, ProviderError> providerErrors) {
        try {
            ProviderEvaluation<T> evaluation = providerFunction.apply(provider);
            if (evaluation == null) {
                providerErrors.put(
                        providerName, ProviderError.fromResult(providerName, ErrorCode.GENERAL, "null evaluation"));
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
    }

    /** Builds a {@link MultiProviderEvaluation} carrying per-provider error details. */
    private <T> ProviderEvaluation<T> errorResult(String baseMessage, List<ProviderError> orderedErrors) {
        return MultiProviderEvaluation.<T>builder()
                .errorCode(ErrorCode.GENERAL)
                .errorMessage(ProviderError.buildAggregateMessage(baseMessage, orderedErrors))
                .providerErrors(orderedErrors)
                .build();
    }

    /**
     * Merges the recorded errors into a single list ordered by provider registration order, so that
     * aggregate messages are stable across runs.
     */
    private List<ProviderError> orderedErrors(
            Map<String, FeatureProvider> providers,
            Map<String, ProviderError> providerErrors,
            Map<String, ProviderError> timeoutErrors) {
        List<ProviderError> orderedErrors = new ArrayList<>(providerErrors.size() + timeoutErrors.size());
        for (String providerName : providers.keySet()) {
            ProviderError error = providerErrors.get(providerName);
            if (error == null) {
                error = timeoutErrors.get(providerName);
            }
            if (error != null) {
                orderedErrors.add(error);
            }
        }
        return orderedErrors;
    }

    /**
     * Returns the successful evaluation as a {@link MultiProviderEvaluation} carrying the given
     * per-provider errors, so callers can see which providers were skipped or timed out.
     */
    private <T> ProviderEvaluation<T> withProviderErrors(
            ProviderEvaluation<T> evaluation, List<ProviderError> orderedErrors) {
        return MultiProviderEvaluation.<T>builder()
                .value(evaluation.getValue())
                .variant(evaluation.getVariant())
                .reason(evaluation.getReason())
                .flagMetadata(evaluation.getFlagMetadata())
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

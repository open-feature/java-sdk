package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Value;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <b>Experimental:</b> Provider implementation for multi-provider.
 *
 * <p>This provider delegates flag evaluations to multiple underlying providers using a configurable
 * {@link Strategy}. It also exposes combined metadata containing the original metadata of each
 * underlying provider.
 */
@Slf4j
public class MultiProvider extends EventProvider {

    @Getter
    private static final String NAME = "multiprovider";

    // Use CPU count as upper bound for init threads.
    public static final int INIT_THREADS_COUNT = Runtime.getRuntime().availableProcessors();

    private final List<FeatureProvider> providers;
    private final Strategy strategy;
    private MultiProviderMetadata metadata;

    /**
     * Constructs a MultiProvider with the given list of FeatureProviders, by default uses
     * {@link FirstMatchStrategy}.
     *
     * @param providers the list of FeatureProviders to initialize the MultiProvider with
     */
    public MultiProvider(List<FeatureProvider> providers) {
        this(providers, new FirstMatchStrategy());
    }

    /**
     * Constructs a MultiProvider with the given list of FeatureProviders and a strategy.
     *
     * @param providers the list of FeatureProviders to initialize the MultiProvider with
     * @param strategy  the strategy (if {@code null}, {@link FirstMatchStrategy} is used)
     */
    public MultiProvider(List<FeatureProvider> providers, Strategy strategy) {
        this.providers = providers;
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
    }

    /**
     * Initialize the provider.
     *
     * @param evaluationContext evaluation context
     * @throws Exception on error (e.g. wrapped {@link java.util.concurrent.ExecutionException}
     *                   from a failing provider)
     */
    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        var metadataBuilder = MultiProviderMetadata.builder().name(NAME);

        if (providers.isEmpty()) {
            metadataBuilder.originalMetadata(Collections.emptyList());
            metadata = metadataBuilder.build();
            return;
        }

        List<Metadata> providersMetadata = new ArrayList<>(providers.size());

        var executorService = Executors.newFixedThreadPool(Math.min(INIT_THREADS_COUNT, providers.size()));
        try {
            Collection<Callable<Void>> tasks = new ArrayList<>(providers.size());
            for (FeatureProvider provider : providers) {
                tasks.add(() -> {
                    provider.initialize(evaluationContext);
                    return null;
                });
                providersMetadata.add(provider.getMetadata());
            }

            metadataBuilder.originalMetadata(Collections.unmodifiableList(providersMetadata));

            List<Future<Void>> results = executorService.invokeAll(tasks);
            for (Future<Void> result : results) {
                // This will re-throw any exception from the provider's initialize method,
                // wrapped in an ExecutionException.
                result.get();
            }
        } catch (Exception e) {
            // If initialization fails for any provider, attempt to shut down via the
            // standard shutdown path to avoid a partial/limbo state.
            try {
                shutdown();
            } catch (Exception shutdownEx) {
                log.error("error during shutdown after failed initialize", shutdownEx);
            }
            throw e;
        } finally {
            executorService.shutdown();
        }

        metadata = metadataBuilder.build();
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP")
    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return strategy.evaluate(
                providers, key, defaultValue, ctx, p -> p.getBooleanEvaluation(key, defaultValue, ctx));
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return strategy.evaluate(providers, key, defaultValue, ctx, p -> p.getStringEvaluation(key, defaultValue, ctx));
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return strategy.evaluate(
                providers, key, defaultValue, ctx, p -> p.getIntegerEvaluation(key, defaultValue, ctx));
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return strategy.evaluate(providers, key, defaultValue, ctx, p -> p.getDoubleEvaluation(key, defaultValue, ctx));
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        return strategy.evaluate(providers, key, defaultValue, ctx, p -> p.getObjectEvaluation(key, defaultValue, ctx));
    }

    @Override
    public void shutdown() {
        log.debug("shutdown begin");
        for (FeatureProvider provider : providers) {
            try {
                provider.shutdown();
            } catch (Exception e) {
                log.error("error shutdown provider {}", provider.getMetadata().getName(), e);
            }
        }
        log.debug("shutdown end");
        // Important: ensure EventProvider's executor is also shut down
        super.shutdown();
    }
}

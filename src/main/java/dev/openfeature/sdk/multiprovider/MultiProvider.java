package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.GeneralError;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** <b>Experimental:</b> Provider implementation for Multi-provider. */
@Slf4j
public class MultiProvider extends EventProvider {

    @Getter
    private static final String NAME = "multiprovider";

    public static final int INIT_THREADS_COUNT = 8;
    private final Map<String, FeatureProvider> providers;
    private final Strategy strategy;
    private MultiProviderMetadata metadata;

    /**
     * Constructs a MultiProvider with the given list of FeatureProviders, by default uses FirstMatchStrategy.
     *
     * @param providers the list of FeatureProviders to initialize the MultiProvider with
     */
    public MultiProvider(List<FeatureProvider> providers) {
        this(providers, null);
    }

    /**
     * Constructs a MultiProvider with the given list of FeatureProviders and a strategy.
     *
     * @param providers the list of FeatureProviders to initialize the MultiProvider with
     * @param strategy the strategy
     */
    public MultiProvider(List<FeatureProvider> providers, Strategy strategy) {
        this.providers = buildProviders(providers);
        if (strategy != null) {
            this.strategy = strategy;
        } else {
            this.strategy = new FirstMatchStrategy();
        }
    }

    protected static Map<String, FeatureProvider> buildProviders(List<FeatureProvider> providers) {
        Map<String, FeatureProvider> providersMap = new LinkedHashMap<>(providers.size());
        for (FeatureProvider provider : providers) {
            FeatureProvider prevProvider =
                    providersMap.put(provider.getMetadata().getName(), provider);
            if (prevProvider != null) {
                log.warn("duplicated provider name: {}", provider.getMetadata().getName());
            }
        }
        return Collections.unmodifiableMap(providersMap);
    }

    /**
     * Initialize the provider.
     *
     * @param evaluationContext evaluation context
     * @throws Exception on error
     */
    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        var metadataBuilder = MultiProviderMetadata.builder();
        metadataBuilder.name(NAME);
        HashMap<String, Metadata> providersMetadata = new HashMap<>();
        ExecutorService initPool = Executors.newFixedThreadPool(Math.min(INIT_THREADS_COUNT, providers.size()));
        Collection<Callable<Boolean>> tasks = new ArrayList<>(providers.size());
        for (FeatureProvider provider : providers.values()) {
            tasks.add(() -> {
                provider.initialize(evaluationContext);
                return true;
            });
            Metadata providerMetadata = provider.getMetadata();
            providersMetadata.put(providerMetadata.getName(), providerMetadata);
        }
        metadataBuilder.originalMetadata(providersMetadata);
        List<Future<Boolean>> results = initPool.invokeAll(tasks);
        for (Future<Boolean> result : results) {
            if (!result.get()) {
                initPool.shutdown();
                throw new GeneralError("init failed");
            }
        }
        initPool.shutdown();
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
        for (FeatureProvider provider : providers.values()) {
            try {
                provider.shutdown();
            } catch (Exception e) {
                log.error("error shutdown provider {}", provider.getMetadata().getName(), e);
            }
        }
        log.debug("shutdown end");
    }
}

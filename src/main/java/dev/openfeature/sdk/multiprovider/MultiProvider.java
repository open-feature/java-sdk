package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ClientMetadata;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.FlagValueType;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Value;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
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
 *
 * <p>Hooks registered on the child providers are executed around each child evaluation, so a child
 * provider's own hooks observe the evaluation it takes part in.
 */
@Slf4j
public class MultiProvider extends EventProvider {

    @Getter
    private static final String NAME = "multiprovider";

    // Use CPU count as upper bound for init threads.
    public static final int INIT_THREADS_COUNT = Runtime.getRuntime().availableProcessors();

    private final Map<String, FeatureProvider> providers;
    private final Strategy strategy;
    private final ThreadLocal<HookExecutionContext> hookExecutionContextThreadLocal = new ThreadLocal<>();
    private final ClientMetadata hookClientMetadata = MultiProvider::getNAME;
    private final MultiProviderHookExecutor hookExecutor = new MultiProviderHookExecutor(hookClientMetadata);
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
        this.providers = buildProviders(providers);
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final List<Hook> providerHooks = List.of(new Hook() {
        @Override
        public Optional before(HookContext ctx, Map hints) {
            hookExecutionContextThreadLocal.set(
                    new HookExecutionContext(ctx.getClientMetadata(), snapshotHints(hints)));
            return Optional.empty();
        }

        @Override
        public void finallyAfter(HookContext ctx, FlagEvaluationDetails details, Map hints) {
            hookExecutionContextThreadLocal.remove();
        }
    });

    /**
     * Returns provider-level hooks for this MultiProvider.
     *
     * <p>Includes a {@code before} hook that captures the {@link ClientMetadata}
     * and hook hints from the SDK's hook lifecycle. This context is then available
     * during per-child-provider hook execution, matching the JS SDK's WeakMap-based
     * approach for passing hook context into the provider evaluation.
     *
     * @return the list of provider hooks
     */
    @Override
    public List<Hook> getProviderHooks() {
        return providerHooks;
    }

    /**
     * Defensively copies the hook hints. {@code FlagEvaluationOptions.hookHints} is backed by a
     * mutable map, and the captured hints may be read from other threads when a strategy evaluates
     * providers in parallel. A plain copy (rather than {@code Map.copyOf}) is used so that hints
     * containing null values are still supported.
     */
    private static Map<String, Object> snapshotHints(Map<String, Object> hints) {
        if (hints == null || hints.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<>(hints));
    }

    protected static Map<String, FeatureProvider> buildProviders(List<FeatureProvider> providers) {
        Map<String, FeatureProvider> providersMap = new LinkedHashMap<>(providers.size());
        for (FeatureProvider provider : providers) {
            FeatureProvider prevProvider =
                    providersMap.put(provider.getMetadata().getName(), provider);
            if (prevProvider != null) {
                log.info("duplicated provider name: {}", provider.getMetadata().getName());
            }
        }
        return Collections.unmodifiableMap(providersMap);
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception on error (e.g. wrapped {@link java.util.concurrent.ExecutionException}
     *                   from a failing provider)
     */
    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        initialize(evaluationContext, null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception on error (e.g. wrapped {@link java.util.concurrent.ExecutionException}
     *                   from a failing provider)
     */
    @Override
    public void initialize(EvaluationContext evaluationContext, String domain) throws Exception {
        var metadataBuilder = MultiProviderMetadata.builder().name(NAME);
        HashMap<String, Metadata> providersMetadata = new HashMap<>();

        if (providers.isEmpty()) {
            metadataBuilder.originalMetadata(Collections.unmodifiableMap(providersMetadata));
            metadata = metadataBuilder.build();
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(INIT_THREADS_COUNT, providers.size()));
        try {
            Collection<Callable<Void>> tasks = new ArrayList<>(providers.size());
            for (FeatureProvider provider : providers.values()) {
                tasks.add(() -> {
                    provider.initialize(evaluationContext, domain);
                    return null;
                });
                Metadata providerMetadata = provider.getMetadata();
                providersMetadata.put(providerMetadata.getName(), providerMetadata);
            }

            metadataBuilder.originalMetadata(Collections.unmodifiableMap(providersMetadata));

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
        HookExecutionContext hookExecutionContext = currentHookExecutionContext();
        return strategy.evaluate(
                providers,
                key,
                defaultValue,
                ctx,
                provider -> hookExecutor.evaluate(
                        provider,
                        key,
                        defaultValue,
                        ctx,
                        hookExecutionContext,
                        FlagValueType.BOOLEAN,
                        (p, evaluationContext) -> p.getBooleanEvaluation(key, defaultValue, evaluationContext)));
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        HookExecutionContext hookExecutionContext = currentHookExecutionContext();
        return strategy.evaluate(
                providers,
                key,
                defaultValue,
                ctx,
                provider -> hookExecutor.evaluate(
                        provider,
                        key,
                        defaultValue,
                        ctx,
                        hookExecutionContext,
                        FlagValueType.STRING,
                        (p, evaluationContext) -> p.getStringEvaluation(key, defaultValue, evaluationContext)));
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        HookExecutionContext hookExecutionContext = currentHookExecutionContext();
        return strategy.evaluate(
                providers,
                key,
                defaultValue,
                ctx,
                provider -> hookExecutor.evaluate(
                        provider,
                        key,
                        defaultValue,
                        ctx,
                        hookExecutionContext,
                        FlagValueType.INTEGER,
                        (p, evaluationContext) -> p.getIntegerEvaluation(key, defaultValue, evaluationContext)));
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        HookExecutionContext hookExecutionContext = currentHookExecutionContext();
        return strategy.evaluate(
                providers,
                key,
                defaultValue,
                ctx,
                provider -> hookExecutor.evaluate(
                        provider,
                        key,
                        defaultValue,
                        ctx,
                        hookExecutionContext,
                        FlagValueType.DOUBLE,
                        (p, evaluationContext) -> p.getDoubleEvaluation(key, defaultValue, evaluationContext)));
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        HookExecutionContext hookExecutionContext = currentHookExecutionContext();
        return strategy.evaluate(
                providers,
                key,
                defaultValue,
                ctx,
                provider -> hookExecutor.evaluate(
                        provider,
                        key,
                        defaultValue,
                        ctx,
                        hookExecutionContext,
                        FlagValueType.OBJECT,
                        (p, evaluationContext) -> p.getObjectEvaluation(key, defaultValue, evaluationContext)));
    }

    private HookExecutionContext currentHookExecutionContext() {
        return hookExecutionContextThreadLocal.get();
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
        // Important: ensure EventProvider's executor is also shut down
        super.shutdown();
    }
}

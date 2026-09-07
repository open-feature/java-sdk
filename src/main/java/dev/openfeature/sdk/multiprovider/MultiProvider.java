package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.ProviderEvent;
import dev.openfeature.sdk.ProviderEventDetails;
import dev.openfeature.sdk.ProviderState;
import dev.openfeature.sdk.TrackingEventDetails;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <b>Experimental:</b> Provider implementation for multi-provider.
 *
 * <p>This provider delegates flag evaluations to multiple underlying providers using a configurable
 * {@link Strategy}. It also exposes combined metadata containing the original metadata of each
 * underlying provider.
 *
 * <p>Child provider events are observed and aggregated into a single provider state using a
 * "worst wins" precedence, and {@code track} calls are forwarded to every usable child provider.
 */
@Slf4j
public class MultiProvider extends EventProvider {

    @Getter
    private static final String NAME = "multiprovider";

    // Use CPU count as upper bound for init threads.
    public static final int INIT_THREADS_COUNT = Runtime.getRuntime().availableProcessors();

    private final Map<String, FeatureProvider> providers;
    private final Strategy strategy;
    private final Map<String, ProviderState> providerStates = new ConcurrentHashMap<>();
    private final Map<String, BiConsumer<ProviderEvent, ProviderEventDetails>> providerEventObservers =
            new ConcurrentHashMap<>();
    private ProviderState aggregateState;
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
        initializeProviderStates();
        this.aggregateState = determineAggregateState();
    }

    /**
     * Builds the internal provider map, deriving a unique name for each provider.
     *
     * <p>Providers that share a metadata name are disambiguated with a numeric suffix
     * ({@code name-1}, {@code name-2}, ...) so that no provider is silently dropped.
     */
    protected static Map<String, FeatureProvider> buildProviders(List<FeatureProvider> providers) {
        Objects.requireNonNull(providers, "providers must not be null");
        Map<String, FeatureProvider> providersMap = new LinkedHashMap<>(providers.size());
        Map<String, Integer> suffixesByBaseName = new HashMap<>(providers.size());
        for (FeatureProvider provider : providers) {
            Objects.requireNonNull(provider, "provider must not be null");
            String baseName = getProviderBaseName(provider);
            String resolvedName = resolveUniqueProviderName(baseName, providersMap, suffixesByBaseName);
            if (!baseName.equals(resolvedName)) {
                log.info("deduplicated provider name from {} to {}", baseName, resolvedName);
            }
            providersMap.put(resolvedName, provider);
        }
        return Collections.unmodifiableMap(providersMap);
    }

    private static String getProviderBaseName(FeatureProvider provider) {
        Metadata providerMetadata = provider.getMetadata();
        if (providerMetadata == null
                || providerMetadata.getName() == null
                || providerMetadata.getName().isEmpty()) {
            return "provider";
        }
        return providerMetadata.getName();
    }

    private static String resolveUniqueProviderName(
            String baseName, Map<String, FeatureProvider> providersMap, Map<String, Integer> suffixesByBaseName) {
        if (!providersMap.containsKey(baseName)) {
            suffixesByBaseName.putIfAbsent(baseName, 1);
            return baseName;
        }
        int suffix = suffixesByBaseName.getOrDefault(baseName, 1);
        String resolvedName = baseName + "-" + suffix;
        while (providersMap.containsKey(resolvedName)) {
            suffix++;
            resolvedName = baseName + "-" + suffix;
        }
        suffixesByBaseName.put(baseName, suffix + 1);
        return resolvedName;
    }

    private void initializeProviderStates() {
        providerStates.clear();
        if (!providers.isEmpty()) {
            for (String providerName : providers.keySet()) {
                providerStates.put(providerName, ProviderState.NOT_READY);
            }
        }
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
        Map<String, Metadata> providersMetadata = new LinkedHashMap<>();
        initializeProviderStates();
        synchronized (this) {
            emitAggregateStateChange(
                    determineAggregateState(), ProviderEventDetails.builder().build());
        }

        if (providers.isEmpty()) {
            metadataBuilder.originalMetadata(Collections.unmodifiableMap(providersMetadata));
            metadata = metadataBuilder.build();
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(INIT_THREADS_COUNT, providers.size()));
        try {
            Collection<Callable<Void>> tasks = new ArrayList<>(providers.size());
            for (Map.Entry<String, FeatureProvider> entry : providers.entrySet()) {
                String providerName = entry.getKey();
                FeatureProvider provider = entry.getValue();
                registerChildProviderObserver(providerName, provider);
                tasks.add(() -> {
                    try {
                        provider.initialize(evaluationContext, domain);
                        setProviderReadyIfStillNotReady(providerName);
                        return null;
                    } catch (Exception e) {
                        setProviderState(providerName, toStateFromException(e), providerErrorDetails(e));
                        throw e;
                    }
                });
                Metadata providerMetadata = provider.getMetadata();
                providersMetadata.put(providerName, providerMetadata);
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

    /**
     * Forwards the tracking event to every child provider that is in a usable state.
     *
     * <p>Providers that are {@code NOT_READY} or {@code FATAL} are skipped. Errors raised by an
     * individual provider are logged and do not prevent the remaining providers from being called.
     */
    @Override
    public void track(String eventName, EvaluationContext context, TrackingEventDetails details) {
        for (Map.Entry<String, FeatureProvider> entry : providers.entrySet()) {
            String providerName = entry.getKey();
            FeatureProvider provider = entry.getValue();
            if (!shouldTrackProvider(providerName)) {
                continue;
            }
            try {
                provider.track(eventName, context, details);
            } catch (Exception e) {
                log.error("error forwarding track to provider {}", providerName, e);
            }
        }
    }

    @Override
    public void shutdown() {
        log.debug("shutdown begin");
        for (Map.Entry<String, FeatureProvider> entry : providers.entrySet()) {
            String providerName = entry.getKey();
            FeatureProvider provider = entry.getValue();
            try {
                unregisterChildProviderObserver(providerName, provider);
                provider.shutdown();
            } catch (Exception e) {
                log.error("error shutdown provider {}", providerName, e);
            }
        }
        synchronized (this) {
            initializeProviderStates();
            emitAggregateStateChange(
                    ProviderState.NOT_READY, ProviderEventDetails.builder().build());
        }
        log.debug("shutdown end");
        // Important: ensure EventProvider's executor is also shut down
        super.shutdown();
    }

    private void registerChildProviderObserver(String providerName, FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            BiConsumer<ProviderEvent, ProviderEventDetails> observer =
                    (event, details) -> onChildProviderEvent(providerName, event, details);
            ((EventProvider) provider).addEventObserver(observer);
            providerEventObservers.put(providerName, observer);
        }
    }

    private void unregisterChildProviderObserver(String providerName, FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            BiConsumer<ProviderEvent, ProviderEventDetails> observer = providerEventObservers.remove(providerName);
            if (observer != null) {
                ((EventProvider) provider).removeEventObserver(observer);
            }
        }
    }

    private void onChildProviderEvent(String providerName, ProviderEvent event, ProviderEventDetails details) {
        if (ProviderEvent.PROVIDER_CONFIGURATION_CHANGED.equals(event)) {
            emitProviderConfigurationChanged(details);
            return;
        }
        ProviderState state = toStateFromEvent(event, details);
        if (state != null) {
            setProviderState(providerName, state, details);
        }
    }

    private synchronized void setProviderState(
            String providerName, ProviderState providerState, ProviderEventDetails details) {
        providerStates.put(providerName, providerState);
        ProviderState aggregate = determineAggregateState();
        emitAggregateStateChange(aggregate, details);
    }

    private synchronized void setProviderReadyIfStillNotReady(String providerName) {
        if (!ProviderState.NOT_READY.equals(providerStates.get(providerName))) {
            return;
        }
        providerStates.put(providerName, ProviderState.READY);
        ProviderState aggregate = determineAggregateState();
        emitAggregateStateChange(aggregate, ProviderEventDetails.builder().build());
    }

    private void emitAggregateStateChange(ProviderState aggregate, ProviderEventDetails details) {
        ProviderState previous = aggregateState;
        if (previous == aggregate) {
            return;
        }
        aggregateState = aggregate;
        switch (aggregate) {
            case READY:
                emitProviderReady(detailsOrEmpty(details));
                break;
            case STALE:
                emitProviderStale(detailsOrEmpty(details));
                break;
            case ERROR:
                emitProviderError(ensureErrorDetails(details, ErrorCode.GENERAL));
                break;
            case FATAL:
                emitProviderError(ensureErrorDetails(details, ErrorCode.PROVIDER_FATAL));
                break;
            case NOT_READY:
                break;
            default:
                break;
        }
    }

    /** Aggregates the child provider states using a "worst wins" precedence. */
    private ProviderState determineAggregateState() {
        if (providerStates.isEmpty()) {
            return ProviderState.READY;
        }
        ProviderState aggregate = ProviderState.READY;
        for (ProviderState state : providerStates.values()) {
            if (stateSeverity(state) > stateSeverity(aggregate)) {
                aggregate = state;
            }
        }
        return aggregate;
    }

    private int stateSeverity(ProviderState state) {
        if (state == null) {
            return 0;
        }
        switch (state) {
            case FATAL:
                return 5;
            case NOT_READY:
                return 4;
            case ERROR:
                return 3;
            case STALE:
                return 2;
            case READY:
                return 1;
            default:
                return 0;
        }
    }

    private ProviderEventDetails detailsOrEmpty(ProviderEventDetails details) {
        if (details == null) {
            return ProviderEventDetails.builder().build();
        }
        return details;
    }

    private ProviderEventDetails ensureErrorDetails(ProviderEventDetails details, ErrorCode defaultErrorCode) {
        if (details == null) {
            return ProviderEventDetails.builder().errorCode(defaultErrorCode).build();
        }
        if (details.getErrorCode() == null) {
            return details.toBuilder().errorCode(defaultErrorCode).build();
        }
        return details;
    }

    private ProviderState toStateFromEvent(ProviderEvent event, ProviderEventDetails details) {
        if (ProviderEvent.PROVIDER_READY.equals(event)) {
            return ProviderState.READY;
        }
        if (ProviderEvent.PROVIDER_STALE.equals(event)) {
            return ProviderState.STALE;
        }
        if (ProviderEvent.PROVIDER_ERROR.equals(event)) {
            if (details != null && ErrorCode.PROVIDER_FATAL.equals(details.getErrorCode())) {
                return ProviderState.FATAL;
            }
            return ProviderState.ERROR;
        }
        return null;
    }

    private ProviderState toStateFromException(Exception exception) {
        if (exception instanceof OpenFeatureError
                && ErrorCode.PROVIDER_FATAL.equals(((OpenFeatureError) exception).getErrorCode())) {
            return ProviderState.FATAL;
        }
        return ProviderState.ERROR;
    }

    private ProviderEventDetails providerErrorDetails(Exception exception) {
        if (exception instanceof OpenFeatureError) {
            ErrorCode errorCode = ((OpenFeatureError) exception).getErrorCode();
            return ProviderEventDetails.builder()
                    .errorCode(errorCode)
                    .message(exception.getMessage())
                    .build();
        }
        return ProviderEventDetails.builder()
                .errorCode(ErrorCode.GENERAL)
                .message(exception.getMessage())
                .build();
    }

    private boolean shouldTrackProvider(String providerName) {
        ProviderState providerState = providerStates.getOrDefault(providerName, ProviderState.READY);
        return !ProviderState.NOT_READY.equals(providerState) && !ProviderState.FATAL.equals(providerState);
    }
}

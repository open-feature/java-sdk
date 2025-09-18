package dev.openfeature.sdk;

import dev.openfeature.api.Client;
import dev.openfeature.api.ClientMetadata;
import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.EventDetails;
import dev.openfeature.api.FeatureProvider;
import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.api.FlagEvaluationOptions;
import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Hook;
import dev.openfeature.api.HookData;
import dev.openfeature.api.ImmutableStructure;
import dev.openfeature.api.Metadata;
import dev.openfeature.api.ProviderEvaluation;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.Reason;
import dev.openfeature.api.TrackingEventDetails;
import dev.openfeature.api.Value;
import dev.openfeature.api.exceptions.ExceptionUtils;
import dev.openfeature.api.exceptions.FatalError;
import dev.openfeature.api.exceptions.GeneralError;
import dev.openfeature.api.exceptions.OpenFeatureError;
import dev.openfeature.api.exceptions.ProviderNotReadyError;
import dev.openfeature.sdk.internal.ObjectUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenFeature Client implementation.
 * You should not instantiate this or reference this class.
 * Use the dev.openfeature.sdk.Client interface instead.
 *
 * @see Client
 */
@SuppressWarnings({
    "PMD.DataflowAnomalyAnalysis",
    "PMD.BeanMembersShouldSerialize",
    "PMD.UnusedLocalVariable",
    "unchecked",
    "rawtypes"
})
class OpenFeatureClient implements Client {
    private static final Logger log = LoggerFactory.getLogger(OpenFeatureClient.class);

    private final DefaultOpenFeatureAPI openfeatureApi;
    private final String domain;
    private final String version;

    public String getDomain() {
        return domain;
    }

    public String getVersion() {
        return version;
    }

    private final ConcurrentLinkedQueue<Hook> clientHooks;
    private final HookSupport hookSupport;
    private final AtomicReference<EvaluationContext> evaluationContext = new AtomicReference<>();

    /**
     * Do not use this constructor. It's for internal use only.
     *      Clients created using it will not run event handlers.
     *      Use the OpenFeatureAPI's getClient factory method instead.
     *
     * @param openFeatureAPI Backing global singleton
     * @param domain         An identifier which logically binds clients with
     *                       providers (used by observability tools).
     * @param version        Version of the client (used by observability tools).
     */
    OpenFeatureClient(DefaultOpenFeatureAPI openFeatureAPI, String domain, String version) {
        this.openfeatureApi = openFeatureAPI;
        this.domain = domain;
        this.version = version;
        this.clientHooks = new ConcurrentLinkedQueue<>();
        this.hookSupport = new HookSupport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProviderState getProviderState() {
        return openfeatureApi.getFeatureProviderStateManager(domain).getState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void track(String trackingEventName) {
        validateTrackingEventName(trackingEventName);
        invokeTrack(trackingEventName, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void track(String trackingEventName, EvaluationContext context) {
        validateTrackingEventName(trackingEventName);
        Objects.requireNonNull(context);
        invokeTrack(trackingEventName, context, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void track(String trackingEventName, TrackingEventDetails details) {
        validateTrackingEventName(trackingEventName);
        Objects.requireNonNull(details);
        invokeTrack(trackingEventName, null, details);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void track(String trackingEventName, EvaluationContext context, TrackingEventDetails details) {
        validateTrackingEventName(trackingEventName);
        Objects.requireNonNull(context);
        Objects.requireNonNull(details);
        invokeTrack(trackingEventName, mergeEvaluationContext(context), details);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureClient addHooks(Hook... hooks) {
        this.clientHooks.addAll(Arrays.asList(hooks));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Hook> getHooks() {
        return new ArrayList<>(this.clientHooks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureClient setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContext.set(evaluationContext);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EvaluationContext getEvaluationContext() {
        return this.evaluationContext.get();
    }

    @SuppressFBWarnings(
            value = {"REC_CATCH_EXCEPTION"},
            justification = "We don't want to allow any exception to reach the user. "
                    + "Instead, we return an evaluation result with the appropriate error code.")
    private <T> FlagEvaluationDetails<T> evaluateFlag(
            FlagValueType type, String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        var flagOptions = ObjectUtils.defaultIfNull(
                options, () -> FlagEvaluationOptions.builder().build());
        var hints = Collections.unmodifiableMap(flagOptions.getHookHints());

        FlagEvaluationDetails<T> details = null;
        List<Hook> mergedHooks;
        List<Pair<Hook, HookData>> hookDataPairs = null;
        HookContextWithoutData<T> hookContext = null;
        ProviderEvaluation<T> providerEval = null;

        try {
            final var stateManager = openfeatureApi.getFeatureProviderStateManager(this.domain);
            // provider must be accessed once to maintain a consistent reference
            final var provider = stateManager.getProvider();
            final var state = stateManager.getState();
            hookContext =
                    HookContextWithoutData.of(key, type, this.getMetadata(), provider.getMetadata(), defaultValue);

            // we are setting the evaluation context one after the other, so that we have a hook context in each
            // possible exception case.
            hookContext.setCtx(mergeEvaluationContext(ctx));

            mergedHooks = ObjectUtils.merge(
                    provider.getProviderHooks(), flagOptions.getHooks(), clientHooks, openfeatureApi.getMutableHooks());
            hookDataPairs = hookSupport.getHookDataPairs(mergedHooks, type);
            var mergedCtx = hookSupport.beforeHooks(type, hookContext, hookDataPairs, hints);
            hookContext.setCtx(mergedCtx);

            // "short circuit" if the provider is in NOT_READY or FATAL state
            if (ProviderState.NOT_READY.equals(state)) {
                throw new ProviderNotReadyError("Provider not yet initialized");
            }
            if (ProviderState.FATAL.equals(state)) {
                throw new FatalError("Provider is in an irrecoverable error state");
            }

            providerEval =
                    (ProviderEvaluation<T>) createProviderEvaluation(type, key, defaultValue, provider, mergedCtx);

            var flagMetadata =
                    Optional.ofNullable(providerEval.getFlagMetadata()).orElseGet(() -> Metadata.EMPTY);
            if (providerEval.getErrorCode() != null) {
                var error = ExceptionUtils.instantiateErrorByErrorCode(
                        providerEval.getErrorCode(), providerEval.getErrorMessage());

                // Create new details with error defaults since object is immutable
                details = FlagEvaluationDetails.of(
                        key,
                        defaultValue,
                        providerEval.getVariant(),
                        Reason.ERROR,
                        providerEval.getErrorCode(),
                        providerEval.getErrorMessage(),
                        flagMetadata);

                hookSupport.errorHooks(type, hookContext, error, hookDataPairs, hints);
            } else {
                details = FlagEvaluationDetails.of(
                        key,
                        providerEval.getValue(),
                        providerEval.getVariant(),
                        providerEval.getReason(),
                        providerEval.getErrorCode(),
                        providerEval.getErrorMessage(),
                        flagMetadata);

                hookSupport.afterHooks(type, hookContext, details, hookDataPairs, hints);
            }
        } catch (Exception e) {
            ErrorCode errorCode =
                    (e instanceof OpenFeatureError) ? ((OpenFeatureError) e).getErrorCode() : ErrorCode.GENERAL;

            details = FlagEvaluationDetails.of(
                    key,
                    defaultValue,
                    (providerEval != null) ? providerEval.getVariant() : null,
                    Reason.ERROR,
                    errorCode,
                    e.getMessage(),
                    Metadata.EMPTY);

            hookSupport.errorHooks(type, hookContext, e, hookDataPairs, hints);
        } finally {
            hookSupport.afterAllHooks(type, hookContext, details, hookDataPairs, hints);
        }

        return details;
    }

    private static void validateTrackingEventName(String str) {
        Objects.requireNonNull(str);
        if (str.isEmpty()) {
            throw new IllegalArgumentException("trackingEventName cannot be empty");
        }
    }

    private void invokeTrack(String trackingEventName, EvaluationContext context, TrackingEventDetails details) {
        openfeatureApi
                .getFeatureProviderStateManager(domain)
                .getProvider()
                .track(trackingEventName, mergeEvaluationContext(context), details);
    }

    /**
     * Merge invocation contexts with API, transaction and client contexts.
     * Does not merge before context.
     *
     * @param invocationContext invocation context
     * @return merged evaluation context
     */
    private EvaluationContext mergeEvaluationContext(EvaluationContext invocationContext) {
        final EvaluationContext apiContext = openfeatureApi.getEvaluationContext();
        final EvaluationContext clientContext = evaluationContext.get();
        final EvaluationContext transactionContext = openfeatureApi.getTransactionContext();
        return mergeContextMaps(apiContext, transactionContext, clientContext, invocationContext);
    }

    private EvaluationContext mergeContextMaps(EvaluationContext... contexts) {
        // avoid any unnecessary context instantiations and stream usage here; this is
        // called with every evaluation.
        Map merged = new HashMap<>();
        for (EvaluationContext evaluationContext : contexts) {
            if (evaluationContext != null && !evaluationContext.isEmpty()) {
                EvaluationContext.mergeMaps(ImmutableStructure::new, merged, evaluationContext.asUnmodifiableMap());
            }
        }
        // TODO: this might add object churn, do we need the immutableContext in the api?
        return EvaluationContext.immutableOf(merged);
    }

    private <T> ProviderEvaluation<?> createProviderEvaluation(
            FlagValueType type,
            String key,
            T defaultValue,
            FeatureProvider provider,
            EvaluationContext invocationContext) {
        switch (type) {
            case BOOLEAN:
                return provider.getBooleanEvaluation(key, (Boolean) defaultValue, invocationContext);
            case STRING:
                return provider.getStringEvaluation(key, (String) defaultValue, invocationContext);
            case INTEGER:
                return provider.getIntegerEvaluation(key, (Integer) defaultValue, invocationContext);
            case DOUBLE:
                return provider.getDoubleEvaluation(key, (Double) defaultValue, invocationContext);
            case OBJECT:
                return provider.getObjectEvaluation(key, (Value) defaultValue, invocationContext);
            default:
                throw new GeneralError("Unknown flag type");
        }
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return getBooleanDetails(key, defaultValue).getValue();
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanDetails(key, defaultValue, ctx).getValue();
    }

    @Override
    public Boolean getBooleanValue(
            String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getBooleanDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue) {
        return getBooleanDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanDetails(
                key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(
            String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.BOOLEAN, key, defaultValue, ctx, options);
    }

    @Override
    public String getStringValue(String key, String defaultValue) {
        return getStringDetails(key, defaultValue).getValue();
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx) {
        return getStringDetails(key, defaultValue, ctx).getValue();
    }

    @Override
    public String getStringValue(
            String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getStringDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue) {
        return getStringDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return getStringDetails(
                key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(
            String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.STRING, key, defaultValue, ctx, options);
    }

    @Override
    public Integer getIntegerValue(String key, Integer defaultValue) {
        return getIntegerDetails(key, defaultValue).getValue();
    }

    @Override
    public Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerDetails(key, defaultValue, ctx).getValue();
    }

    @Override
    public Integer getIntegerValue(
            String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getIntegerDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue) {
        return getIntegerDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerDetails(
                key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(
            String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, ctx, options);
    }

    @Override
    public Double getDoubleValue(String key, Double defaultValue) {
        return getDoubleValue(key, defaultValue, null);
    }

    @Override
    public Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx) {
        return getDoubleValue(key, defaultValue, ctx, null);
    }

    @Override
    public Double getDoubleValue(
            String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.DOUBLE, key, defaultValue, ctx, options)
                .getValue();
    }

    @Override
    public FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue) {
        return getDoubleDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue, EvaluationContext ctx) {
        return getDoubleDetails(key, defaultValue, ctx, null);
    }

    @Override
    public FlagEvaluationDetails<Double> getDoubleDetails(
            String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.DOUBLE, key, defaultValue, ctx, options);
    }

    @Override
    public Value getObjectValue(String key, Value defaultValue) {
        return getObjectDetails(key, defaultValue).getValue();
    }

    @Override
    public Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx) {
        return getObjectDetails(key, defaultValue, ctx).getValue();
    }

    @Override
    public Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getObjectDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue) {
        return getObjectDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue, EvaluationContext ctx) {
        return getObjectDetails(
                key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(
            String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.OBJECT, key, defaultValue, ctx, options);
    }

    @Override
    public ClientMetadata getMetadata() {
        return () -> domain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client onProviderReady(Consumer<EventDetails> handler) {
        return on(ProviderEvent.PROVIDER_READY, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client onProviderConfigurationChanged(Consumer<EventDetails> handler) {
        return on(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client onProviderError(Consumer<EventDetails> handler) {
        return on(ProviderEvent.PROVIDER_ERROR, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client onProviderStale(Consumer<EventDetails> handler) {
        return on(ProviderEvent.PROVIDER_STALE, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client on(ProviderEvent event, Consumer<EventDetails> handler) {
        openfeatureApi.addHandler(domain, event, handler);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        openfeatureApi.removeHandler(domain, event, handler);
        return this;
    }
}

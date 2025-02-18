package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.ExceptionUtils;
import dev.openfeature.sdk.exceptions.FatalError;
import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.exceptions.ProviderNotReadyError;
import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import dev.openfeature.sdk.internal.ObjectUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenFeature Client implementation.
 * You should not instantiate this or reference this class.
 * Use the dev.openfeature.sdk.Client interface instead.
 *
 * @see Client
 * @deprecated // TODO: eventually we will make this non-public. See issue #872
 */
@Slf4j
@SuppressWarnings({
    "PMD.DataflowAnomalyAnalysis",
    "PMD.BeanMembersShouldSerialize",
    "PMD.UnusedLocalVariable",
    "unchecked",
    "rawtypes"
})
@Deprecated() // TODO: eventually we will make this non-public. See issue #872
public class OpenFeatureClient implements Client {

    private final OpenFeatureAPI openfeatureApi;

    @Getter
    private final String domain;

    @Getter
    private final String version;

    private final List<Hook> clientHooks;
    private final HookSupport hookSupport;
    AutoCloseableReentrantReadWriteLock hooksLock = new AutoCloseableReentrantReadWriteLock();
    AutoCloseableReentrantReadWriteLock contextLock = new AutoCloseableReentrantReadWriteLock();
    private EvaluationContext evaluationContext;

    /**
     * Deprecated public constructor. Use OpenFeature.API.getClient() instead.
     *
     * @param openFeatureAPI Backing global singleton
     * @param domain         An identifier which logically binds clients with
     *                       providers (used by observability tools).
     * @param version        Version of the client (used by observability tools).
     * @deprecated Do not use this constructor. It's for internal use only.
     *         Clients created using it will not run event handlers.
     *         Use the OpenFeatureAPI's getClient factory method instead.
     */
    @Deprecated() // TODO: eventually we will make this non-public. See issue #872
    public OpenFeatureClient(OpenFeatureAPI openFeatureAPI, String domain, String version) {
        this.openfeatureApi = openFeatureAPI;
        this.domain = domain;
        this.version = version;
        this.clientHooks = new ArrayList<>();
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
        try (AutoCloseableLock __ = this.hooksLock.writeLockAutoCloseable()) {
            this.clientHooks.addAll(Arrays.asList(hooks));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Hook> getHooks() {
        try (AutoCloseableLock __ = this.hooksLock.readLockAutoCloseable()) {
            return this.clientHooks;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureClient setEvaluationContext(EvaluationContext evaluationContext) {
        try (AutoCloseableLock __ = contextLock.writeLockAutoCloseable()) {
            this.evaluationContext = evaluationContext;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EvaluationContext getEvaluationContext() {
        try (AutoCloseableLock __ = contextLock.readLockAutoCloseable()) {
            return this.evaluationContext;
        }
    }

    private <T> FlagEvaluationDetails<T> evaluateFlag(
            FlagValueType type, String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        FlagEvaluationOptions flagOptions = ObjectUtils.defaultIfNull(
                options, () -> FlagEvaluationOptions.builder().build());
        Map<String, Object> hints = Collections.unmodifiableMap(flagOptions.getHookHints());

        FlagEvaluationDetails<T> details = null;
        List<Hook> mergedHooks = null;
        HookContext<T> afterHookContext = null;
        FeatureProvider provider;

        try {
            FeatureProviderStateManager stateManager = openfeatureApi.getFeatureProviderStateManager(this.domain);
            // provider must be accessed once to maintain a consistent reference
            provider = stateManager.getProvider();
            ProviderState state = stateManager.getState();
            if (ProviderState.NOT_READY.equals(state)) {
                throw new ProviderNotReadyError("provider not yet initialized");
            }
            if (ProviderState.FATAL.equals(state)) {
                throw new FatalError("provider is in an irrecoverable error state");
            }

            mergedHooks = ObjectUtils.merge(
                    provider.getProviderHooks(), flagOptions.getHooks(), clientHooks, openfeatureApi.getHooks());

            EvaluationContext mergedCtx = hookSupport.beforeHooks(
                    type,
                    HookContext.from(
                            key,
                            type,
                            this.getMetadata(),
                            provider.getMetadata(),
                            mergeEvaluationContext(ctx),
                            defaultValue),
                    mergedHooks,
                    hints);

            afterHookContext =
                    HookContext.from(key, type, this.getMetadata(), provider.getMetadata(), mergedCtx, defaultValue);

            ProviderEvaluation<T> providerEval =
                    (ProviderEvaluation<T>) createProviderEvaluation(type, key, defaultValue, provider, mergedCtx);

            details = FlagEvaluationDetails.from(providerEval, key);
            if (details.getErrorCode() != null) {
                OpenFeatureError error =
                        ExceptionUtils.instantiateErrorByErrorCode(details.getErrorCode(), details.getErrorMessage());
                enrichDetailsWithErrorDefaults(defaultValue, details);
                hookSupport.errorHooks(type, afterHookContext, error, mergedHooks, hints);
            } else {
                hookSupport.afterHooks(type, afterHookContext, details, mergedHooks, hints);
            }
        } catch (Exception e) {
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().flagKey(key).build();
            }
            if (e instanceof OpenFeatureError) {
                details.setErrorCode(((OpenFeatureError) e).getErrorCode());
            } else {
                details.setErrorCode(ErrorCode.GENERAL);
            }
            details.setErrorMessage(e.getMessage());
            enrichDetailsWithErrorDefaults(defaultValue, details);
            hookSupport.errorHooks(type, afterHookContext, e, mergedHooks, hints);
        } finally {
            hookSupport.afterAllHooks(type, afterHookContext, details, mergedHooks, hints);
        }

        return details;
    }

    private static <T> void enrichDetailsWithErrorDefaults(T defaultValue, FlagEvaluationDetails<T> details) {
        details.setValue(defaultValue);
        details.setReason(Reason.ERROR.toString());
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
        final EvaluationContext clientContext = this.getEvaluationContext();
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
        return new ImmutableContext(merged);
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

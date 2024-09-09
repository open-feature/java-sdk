package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.*;
import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import dev.openfeature.sdk.internal.ObjectUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

/**
 * OpenFeature Client implementation.
 * You should not instantiate this or reference this class.
 * Use the dev.openfeature.sdk.Client interface instead.
 *
 * @see Client
 * @deprecated // TODO: eventually we will make this non-public. See issue #872
 */
@Slf4j
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize",
        "PMD.UnusedLocalVariable", "unchecked", "rawtypes"})
@Deprecated() // TODO: eventually we will make this non-public. See issue #872
public class OpenFeatureClient implements Client {

    private final ProviderAccessor providerAccessor;
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
     * @param domain         An identifier which logically binds clients with providers (used by observability tools).
     * @param version        Version of the client (used by observability tools).
     * @deprecated Do not use this constructor. It's for internal use only.
     * Clients created using it will not run event handlers.
     * Use the OpenFeatureAPI's getClient factory method instead.
     */
    @Deprecated() // TODO: eventually we will make this non-public. See issue #872
    public OpenFeatureClient(
            ProviderAccessor providerAccessor,
            OpenFeatureAPI openFeatureAPI,
            String domain,
            String version
    ) {
        this.providerAccessor = providerAccessor;
        this.openfeatureApi = openFeatureAPI;
        this.domain = domain;
        this.version = version;
        this.clientHooks = new ArrayList<>();
        this.hookSupport = new HookSupport();
    }

    @Override
    public ProviderState getProviderState() {
        return providerAccessor.getProvider().getState();
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

    private <T> FlagEvaluationDetails<T> evaluateFlag(FlagValueType type, String key, T defaultValue,
                                                      EvaluationContext ctx, FlagEvaluationOptions options) {
        FlagEvaluationOptions flagOptions = ObjectUtils.defaultIfNull(options,
                () -> FlagEvaluationOptions.builder().build());
        Map<String, Object> hints = Collections.unmodifiableMap(flagOptions.getHookHints());
        ctx = ObjectUtils.defaultIfNull(ctx, () -> new ImmutableContext());

        FlagEvaluationDetails<T> details = null;
        List<Hook> mergedHooks = null;
        HookContext<T> afterHookContext = null;
        FeatureProvider provider;

        try {
            // openfeatureApi.getProvider() must be called once to maintain a consistent reference
            provider = providerAccessor.getProvider();
            if (ProviderState.NOT_READY.equals(provider.getState())) {
                throw new ProviderNotReadyError("provider not yet initialized");
            }
            if (ProviderState.FATAL.equals(provider.getState())) {
                throw new FatalError("provider is in an irrecoverable error state");
            }

            mergedHooks = ObjectUtils.merge(provider.getProviderHooks(), flagOptions.getHooks(), clientHooks,
                    openfeatureApi.getHooks());

            EvaluationContext mergedCtx = hookSupport.beforeHooks(type, HookContext.from(key, type, this.getMetadata(),
                    provider.getMetadata(), mergeEvaluationContext(ctx), defaultValue), mergedHooks, hints);

            afterHookContext = HookContext.from(key, type, this.getMetadata(),
                    provider.getMetadata(), mergedCtx, defaultValue);

            ProviderEvaluation<T> providerEval = (ProviderEvaluation<T>) createProviderEvaluation(type, key,
                    defaultValue, provider, mergedCtx);

            details = FlagEvaluationDetails.from(providerEval, key);
            if (details.getErrorCode() != null) {
                throw ExceptionUtils.instantiateErrorByErrorCode(details.getErrorCode(), details.getErrorMessage());
            } else {
                hookSupport.afterHooks(type, afterHookContext, details, mergedHooks, hints);
            }
        } catch (Exception e) {
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().build();
            }
            if (e instanceof OpenFeatureError) {
                details.setErrorCode(((OpenFeatureError) e).getErrorCode());
            } else {
                details.setErrorCode(ErrorCode.GENERAL);
            }
            details.setErrorMessage(e.getMessage());
            details.setValue(defaultValue);
            details.setReason(Reason.ERROR.toString());
            hookSupport.errorHooks(type, afterHookContext, e, mergedHooks, hints);
        } finally {
            hookSupport.afterAllHooks(type, afterHookContext, mergedHooks, hints);
        }

        return details;
    }

    /**
     * Merge invocation contexts with API, transaction and client contexts.
     * Does not merge before context.
     *
     * @param invocationContext invocation context
     * @return merged evaluation context
     */
    private EvaluationContext mergeEvaluationContext(EvaluationContext invocationContext) {
        final EvaluationContext apiContext = openfeatureApi.getEvaluationContext() != null
                ? openfeatureApi.getEvaluationContext()
                : new ImmutableContext();
        final EvaluationContext clientContext = this.getEvaluationContext() != null
                ? this.getEvaluationContext()
                : new ImmutableContext();
        final EvaluationContext transactionContext = openfeatureApi.getTransactionContext() != null
                ? openfeatureApi.getTransactionContext()
                : new ImmutableContext();

        return apiContext.merge(transactionContext.merge(clientContext.merge(invocationContext)));
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
    public Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx,
                                   FlagEvaluationOptions options) {
        return getBooleanDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue) {
        return getBooleanDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx,
                                                            FlagEvaluationOptions options) {
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
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx,
                                 FlagEvaluationOptions options) {
        return getStringDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue) {
        return getStringDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return getStringDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx,
                                                          FlagEvaluationOptions options) {
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
    public Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx,
                                   FlagEvaluationOptions options) {
        return getIntegerDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue) {
        return getIntegerDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx,
                                                            FlagEvaluationOptions options) {
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
    public Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx,
                                 FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.DOUBLE, key, defaultValue, ctx, options).getValue();
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
    public FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue, EvaluationContext ctx,
                                                          FlagEvaluationOptions options) {
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
    public Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx,
                                FlagEvaluationOptions options) {
        return getObjectDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue) {
        return getObjectDetails(key, defaultValue, null);
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue,
                                                         EvaluationContext ctx) {
        return getObjectDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue, EvaluationContext ctx,
                                                         FlagEvaluationOptions options) {
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
        OpenFeatureAPI.getInstance().addHandler(domain, event, handler);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        OpenFeatureAPI.getInstance().removeHandler(domain, event, handler);
        return this;
    }
}

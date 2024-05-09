package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import dev.openfeature.sdk.internal.ObjectUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@inheritDoc}
 */
@Slf4j
@SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize", "unchecked", "rawtypes" })
public class OpenFeatureClient implements Client {

    private final OpenFeatureAPI openfeatureApi;
    @Getter
    private final String name;
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
     *             Clients created using it will not run event handlers.
     *             Use the OpenFeatureAPI's getClient factory method instead.
     */
    @Deprecated() // TODO: eventually we will make this non-public. See issue #872
    public OpenFeatureClient(OpenFeatureAPI openFeatureAPI, String domain, String version) {
        this.openfeatureApi = openFeatureAPI;
        this.name = domain;
        this.domain = domain;
        this.version = version;
        this.clientHooks = new ArrayList<>();
        this.hookSupport = new HookSupport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHooks(Hook... hooks) {
        try (AutoCloseableLock __ = this.hooksLock.writeLockAutoCloseable()) {
            this.clientHooks.addAll(Arrays.asList(hooks));
        }
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
    public void setEvaluationContext(EvaluationContext evaluationContext) {
        try (AutoCloseableLock __ = contextLock.writeLockAutoCloseable()) {
            this.evaluationContext = evaluationContext;
        }
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
        HookContext<T> hookCtx = null;
        FeatureProvider provider;

        try {
            // openfeatureApi.getProvider() must be called once to maintain a consistent reference
            provider = openfeatureApi.getProvider(this.name);

            mergedHooks = ObjectUtils.merge(provider.getProviderHooks(), flagOptions.getHooks(), clientHooks,
                    openfeatureApi.getHooks());

            hookCtx = HookContext.from(key, type, this.getMetadata(),
                    provider.getMetadata(), ctx, defaultValue);

            EvaluationContext ctxFromHook = hookSupport.beforeHooks(type, hookCtx, mergedHooks, hints);

            EvaluationContext mergedCtx = mergeEvaluationContext(ctxFromHook, ctx);

            ProviderEvaluation<T> providerEval = (ProviderEvaluation<T>) createProviderEvaluation(type, key,
                    defaultValue, provider, mergedCtx);

            details = FlagEvaluationDetails.from(providerEval, key);
            hookSupport.afterHooks(type, hookCtx, details, mergedHooks, hints);
        } catch (Exception e) {
            log.error("Unable to correctly evaluate flag with key '{}'", key, e);
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
            hookSupport.errorHooks(type, hookCtx, e, mergedHooks, hints);
        } finally {
            hookSupport.afterAllHooks(type, hookCtx, mergedHooks, hints);
        }

        return details;
    }

    /**
     * Merge hook and invocation contexts with API, transaction and client contexts.
     *
     * @param hookContext       hook context
     * @param invocationContext invocation context
     * @return merged evaluation context
     */
    private EvaluationContext mergeEvaluationContext(
            EvaluationContext hookContext,
            EvaluationContext invocationContext) {
        final EvaluationContext apiContext = openfeatureApi.getEvaluationContext() != null
                ? openfeatureApi.getEvaluationContext()
                : new ImmutableContext();
        final EvaluationContext clientContext = this.getEvaluationContext() != null
                ? this.getEvaluationContext()
                : new ImmutableContext();
        final EvaluationContext transactionContext = openfeatureApi.getTransactionContext() != null
                ? openfeatureApi.getTransactionContext()
                : new ImmutableContext();

        return apiContext.merge(transactionContext.merge(clientContext.merge(invocationContext.merge(hookContext))));
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
    public Metadata getMetadata() {
        return new Metadata() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDomain() {
                return domain;
            }
        };
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
        OpenFeatureAPI.getInstance().addHandler(name, event, handler);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        OpenFeatureAPI.getInstance().removeHandler(name, event, handler);
        return this;
    }
}

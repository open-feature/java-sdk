package dev.openfeature.javasdk;

import java.util.*;

import dev.openfeature.javasdk.exceptions.GeneralError;
import dev.openfeature.javasdk.internal.ObjectUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize", "unchecked", "rawtypes"})
public class OpenFeatureClient implements Client {

    private final OpenFeatureAPI openfeatureApi;
    @Getter private final String name;
    @Getter private final String version;
    @Getter private final List<Hook> clientHooks;
    private final HookSupport hookSupport;

    public OpenFeatureClient(OpenFeatureAPI openFeatureAPI, String name, String version) {
        this.openfeatureApi = openFeatureAPI;
        this.name = name;
        this.version = version;
        this.clientHooks = new ArrayList<>();
        this.hookSupport = new HookSupport();
    }

    @Override
    public void addHooks(Hook... hooks) {
        this.clientHooks.addAll(Arrays.asList(hooks));
    }

    private <T> FlagEvaluationDetails<T> evaluateFlag(FlagValueType type, String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        FlagEvaluationOptions flagOptions = ObjectUtils.defaultIfNull(options, () -> FlagEvaluationOptions.builder().build());
        FeatureProvider provider = openfeatureApi.getProvider();
        Map<String, Object> hints = Collections.unmodifiableMap(flagOptions.getHookHints());
        if (ctx == null) {
            ctx = new EvaluationContext();
        }

        // merge of: API.context, client.context, invocation.context
        HookContext<T> hookCtx = HookContext.from(key, type, this.getMetadata(), openfeatureApi.getProvider().getMetadata(), ctx, defaultValue);

        List<Hook> mergedHooks = ObjectUtils.merge(provider.getProviderHooks(), flagOptions.getHooks(), clientHooks, openfeatureApi.getApiHooks());

        FlagEvaluationDetails<T> details = null;
        try {
            EvaluationContext ctxFromHook = hookSupport.beforeHooks(type, hookCtx, mergedHooks, hints);
            EvaluationContext invocationContext = EvaluationContext.merge(ctxFromHook, ctx);

            ProviderEvaluation<T> providerEval = (ProviderEvaluation<T>) createProviderEvaluation(type, key, defaultValue, options, provider, invocationContext);

            details = FlagEvaluationDetails.from(providerEval, key);
            hookSupport.afterHooks(type, hookCtx, details, mergedHooks, hints);
        } catch (Exception e) {
            log.error("Unable to correctly evaluate flag with key {} due to exception {}", key, e.getMessage());
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().build();
            }
            details.setValue(defaultValue);
            details.setReason(Reason.ERROR);
            details.setErrorCode(e.getMessage());
            hookSupport.errorHooks(type, hookCtx, e, mergedHooks, hints);
        } finally {
            hookSupport.afterAllHooks(type, hookCtx, mergedHooks, hints);
        }

        return details;
    }

    private <T> ProviderEvaluation<?> createProviderEvaluation(
        FlagValueType type,
        String key,
        T defaultValue,
        FlagEvaluationOptions options,
        FeatureProvider provider,
        EvaluationContext invocationContext
    ) {
        switch (type) {
            case BOOLEAN:
                return provider.getBooleanEvaluation(key, (Boolean) defaultValue, invocationContext, options);
            case STRING:
                return provider.getStringEvaluation(key, (String) defaultValue, invocationContext, options);
            case INTEGER:
                return provider.getIntegerEvaluation(key, (Integer) defaultValue, invocationContext, options);
            case DOUBLE:
                return provider.getDoubleEvaluation(key, (Double) defaultValue, invocationContext, options);
            case OBJECT:
                return provider.getObjectEvaluation(key, defaultValue, invocationContext, options);
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
    public Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getBooleanDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue) {
        return getBooleanDetails(key, defaultValue, new EvaluationContext());
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
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
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
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
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
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
    public Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getIntegerDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue) {
        return getIntegerDetails(key, defaultValue, new EvaluationContext());
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
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
    public Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
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
    public FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.DOUBLE, key, defaultValue, ctx, options);
    }

    @Override
    public <T> T getObjectValue(String key, T defaultValue) {
        return getObjectDetails(key, defaultValue).getValue();
    }

    @Override
    public <T> T getObjectValue(String key, T defaultValue, EvaluationContext ctx) {
        return getObjectDetails(key, defaultValue, ctx).getValue();
    }

    @Override
    public <T> T getObjectValue(String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getObjectDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public <T> FlagEvaluationDetails<T> getObjectDetails(String key, T defaultValue) {
        return getObjectDetails(key, defaultValue, null);
    }

    @Override
    public <T> FlagEvaluationDetails<T> getObjectDetails(String key, T defaultValue, EvaluationContext ctx) {
        return getObjectDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    @Override
    public <T> FlagEvaluationDetails<T> getObjectDetails(String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.OBJECT, key, defaultValue, ctx, options);
    }

    @Override
    public Metadata getMetadata() {
        return () -> name;
    }
}

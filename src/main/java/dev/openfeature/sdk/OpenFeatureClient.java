package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.internal.ObjectUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize", "unchecked", "rawtypes"})
public class OpenFeatureClient implements Client {

    private final OpenFeatureAPI openfeatureApi;
    @Getter
    private final String name;
    @Getter
    private final String version;
    @Getter
    private final List<Hook> clientHooks;
    private final HookSupport hookSupport;

    @Getter
    @Setter
    private EvaluationContext evaluationContext;

    /**
     * Client for evaluating the flag. There may be multiples of these floating around.
     * @param openFeatureAPI Backing global singleton
     * @param name Name of the client (used by observability tools).
     * @param version Version of the client (used by observability tools).
     */
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

    private <T> FlagEvaluationDetails<T> evaluateFlag(FlagValueType type, String key, T defaultValue,
        EvaluationContext ctx, FlagEvaluationOptions options) {
        FlagEvaluationOptions flagOptions = ObjectUtils.defaultIfNull(options,
            () -> FlagEvaluationOptions.builder().build());
        Map<String, Object> hints = Collections.unmodifiableMap(flagOptions.getHookHints());
        ctx = ObjectUtils.defaultIfNull(ctx, () -> new MutableContext());
        FeatureProvider provider = ObjectUtils.defaultIfNull(openfeatureApi.getProvider(), () -> {
            log.debug("No provider configured, using no-op provider.");
            return new NoOpProvider();
        });

        FlagEvaluationDetails<T> details = null;
        List<Hook> mergedHooks = null;
        HookContext<T> hookCtx = null;

        try {

            hookCtx = HookContext.from(key, type, this.getMetadata(),
                openfeatureApi.getProvider().getMetadata(), ctx, defaultValue);

            mergedHooks = ObjectUtils.merge(provider.getProviderHooks(), flagOptions.getHooks(), clientHooks,
                openfeatureApi.getApiHooks());

            EvaluationContext ctxFromHook = hookSupport.beforeHooks(type, hookCtx, mergedHooks, hints);

            EvaluationContext invocationCtx = EvaluationContext.merge(ctx, ctxFromHook);

            // merge of: API.context, client.context, invocation.context
            EvaluationContext mergedCtx = EvaluationContext.merge(
                EvaluationContext.merge(
                            openfeatureApi.getEvaluationContext(),
                            this.getEvaluationContext()
                    ),
                    invocationCtx
            );

            ProviderEvaluation<T> providerEval = (ProviderEvaluation<T>) createProviderEvaluation(type, key,
                    defaultValue, provider, mergedCtx);

            details = FlagEvaluationDetails.from(providerEval, key);
            hookSupport.afterHooks(type, hookCtx, details, mergedHooks, hints);
        } catch (Exception e) {
            log.error("Unable to correctly evaluate flag with key {} due to exception {}", key, e.getMessage());
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().build();
            }
            if (e instanceof OpenFeatureError) {
                details.setErrorCode(((OpenFeatureError)e).getErrorCode());
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

    private <T> ProviderEvaluation<?> createProviderEvaluation(
            FlagValueType type,
            String key,
            T defaultValue,
            FeatureProvider provider,
            EvaluationContext invocationContext
    ) {
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
        return getBooleanDetails(key, defaultValue, new MutableContext());
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
        return getIntegerDetails(key, defaultValue, new MutableContext());
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
        return () -> name;
    }
}

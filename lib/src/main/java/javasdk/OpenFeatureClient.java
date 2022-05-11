package javasdk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import javasdk.exceptions.GeneralError;
import javasdk.exceptions.OpenFeatureError;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class OpenFeatureClient implements Client {
    private transient final OpenFeatureAPI openfeatureApi;
    @Getter private final String name;
    @Getter private final String version;
    @Getter private final List<Hook> clientHooks;
    private static final Logger log = LoggerFactory.getLogger(OpenFeatureClient.class);

    public OpenFeatureClient(OpenFeatureAPI openFeatureAPI, String name, String version) {
        this.openfeatureApi = openFeatureAPI;
        this.name = name;
        this.version = version;
        this.clientHooks = new ArrayList<>();
    }

    @Override
    public void registerHooks(Hook... hooks) {
        this.clientHooks.addAll(Arrays.asList(hooks));
    }

    <T> FlagEvaluationDetails<T> evaluateFlag(FlagValueType type, String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        FeatureProvider provider = this.openfeatureApi.getProvider();
        if (ctx == null) {
            ctx = new EvaluationContext();
        }

        ImmutableMap<String, Object> hints = options.getHookHints();

        // TODO: Context transformation?
        HookContext hookCtx = HookContext.from(key, type, this, ctx, defaultValue);

        List<Hook> mergedHooks;
        if (options != null && options.getHooks() != null) {
            mergedHooks = ImmutableList.<Hook>builder()
                    .addAll(options.getHooks())
                    .addAll(clientHooks)
                    .addAll(openfeatureApi.getApiHooks())
                    .build();
        } else {
            mergedHooks = clientHooks;
        }

        FlagEvaluationDetails<T> details = null;
        try {
            this.beforeHooks(hookCtx, mergedHooks, hints);

            ProviderEvaluation<T> providerEval;
            if (type == FlagValueType.BOOLEAN) {
                // TODO: Can we guarantee that defaultValue is a boolean? If not, how to we handle that?
                providerEval = (ProviderEvaluation<T>) provider.getBooleanEvaluation(key, (Boolean) defaultValue, ctx, options);
            } else {
                throw new GeneralError("Unknown flag type");
            }

            details = FlagEvaluationDetails.from(providerEval, key);
            this.afterHooks(hookCtx, details, mergedHooks, hints);
        } catch (Exception e) {
            log.error("Unable to correctly evaluate flag with key {} due to exception {}", key, e.getMessage());
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().value(defaultValue).reason(Reason.ERROR).build();
            }
            details.value = defaultValue;
            details.reason = Reason.ERROR;
            if (e instanceof OpenFeatureError) { //NOPMD - suppressed AvoidInstanceofChecksInCatchClause - Don't want to duplicate detail creation logic.
                details.errorCode = ((OpenFeatureError) e).getErrorCode();
            } else {
                details.errorCode = ErrorCode.GENERAL;
            }
            this.errorHooks(hookCtx, e, mergedHooks, hints);
        } finally {
            this.afterAllHooks(hookCtx, mergedHooks, hints);
        }

        return details;
    }

    private void errorHooks(HookContext hookCtx, Exception e, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        for (Hook hook : hooks) {
            hook.error(hookCtx, e, hints);
        }
    }

    private void afterAllHooks(HookContext hookCtx, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        for (Hook hook : hooks) {
            hook.finallyAfter(hookCtx, hints);
        }
    }

    private <T> void afterHooks(HookContext hookContext, FlagEvaluationDetails<T> details, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        for (Hook hook : hooks) {
            hook.after(hookContext, details, hints);
        }
    }

    private HookContext beforeHooks(HookContext hookCtx, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        // These traverse backwards from normal.
        for (Hook hook : Lists.reverse(hooks)) {
            hook.before(hookCtx, hints);
            // TODO: Merge returned context w/ hook context object
        }
        return hookCtx;
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
        return getBooleanDetails(key, defaultValue, ctx, null);
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.BOOLEAN, key, defaultValue, ctx, options);
    }

    @Override
    public String getStringValue(String key, String defaultValue) {
        return this.getStringDetails(key, defaultValue).getValue();
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx) {
        return this.getStringDetails(key, defaultValue, ctx).getValue();
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.getStringDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue) {
        return getStringDetails(key, defaultValue,  new EvaluationContext());
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return getStringDetails(key, defaultValue,  new EvaluationContext(), null);
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.STRING, key, defaultValue, ctx, options);
    }

    @Override
    public Integer getIntegerValue(String key, Integer defaultValue) {
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, null, null).getValue();
    }

    @Override
    public Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx) {
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, ctx, null).getValue();
    }

    @Override
    public Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue) {
        return getIntegerDetails(key, defaultValue, new EvaluationContext());
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerDetails(key, defaultValue, new EvaluationContext(), null);
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, ctx, options);
    }
}

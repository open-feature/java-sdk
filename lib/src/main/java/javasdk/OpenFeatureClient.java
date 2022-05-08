package javasdk;

import com.google.common.collect.ImmutableList;
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
    private final Logger log = LoggerFactory.getLogger(OpenFeatureClient.class);

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

    private <T> FlagEvaluationDetails<T> evaluateFlag(FlagValueType type, String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        FeatureProvider provider = this.openfeatureApi.getProvider();
        // TODO: Context transformation?
        HookContext hookCtx = HookContext.from(key, type, this, ctx, defaultValue);

        List<Hook> mergedHooks;
        if (options != null && options.getHooks() != null) {
            mergedHooks = ImmutableList.<Hook>builder()
                    .addAll(options.getHooks())
                    .addAll(clientHooks)
                    .build();
        } else {
            mergedHooks = clientHooks;
        }

        FlagEvaluationDetails<T> details = null;
        try {
            this.beforeHooks(hookCtx, mergedHooks);

            ProviderEvaluation<T> providerEval;
            if (type == FlagValueType.BOOLEAN) {
                // TODO: Can we guarantee that defaultValue is a boolean? If not, how to we handle that?
                providerEval = (ProviderEvaluation<T>) provider.getBooleanEvaluation(key, (Boolean) defaultValue, ctx, options);
            } else {
                throw new GeneralError("Unknown flag type");
            }

            details = FlagEvaluationDetails.from(providerEval, key, null);
            this.afterHooks(hookCtx, details, mergedHooks);
        } catch (Exception e) {
            log.error("Unable to correctly evaluate flag with key {} due to exception {}", key, e.getMessage());
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().value(defaultValue).reason(Reason.ERROR).build();
            }
            details.value = defaultValue;
            details.reason = Reason.ERROR;
            if (e instanceof OpenFeatureError) {
                details.errorCode = ((OpenFeatureError) e).getErrorCode();
            } else {
                details.errorCode = ErrorCode.GENERAL;
            }
        } finally {
            this.afterAllHooks(hookCtx, mergedHooks);
        }

        return details;
    }

    private void afterAllHooks(HookContext hookCtx, List<Hook> hooks) {
        for (Hook hook : hooks) {
            hook.afterAll(hookCtx);
        }
    }

    private <T> void afterHooks(HookContext hookContext, FlagEvaluationDetails<T> details, List<Hook> hooks) {
        for (Hook hook : hooks) {
            hook.after(hookContext, details);
        }
    }

    private HookContext beforeHooks(HookContext hookCtx, List<Hook> hooks) {
        for (Hook hook : hooks) {
            hook.before(hookCtx);
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
        return getBooleanDetails(key, defaultValue, null, null);
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
        return this.evaluateFlag(FlagValueType.STRING, key, defaultValue, null, null);
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return this.evaluateFlag(FlagValueType.STRING, key, defaultValue, ctx, null);
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
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, null, null);
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx) {
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, ctx, null);
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.INTEGER, key, defaultValue, ctx, options);
    }
}

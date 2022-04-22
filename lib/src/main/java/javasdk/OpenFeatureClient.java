package javasdk;

import com.google.common.collect.ImmutableList;
import javasdk.exceptions.GeneralError;
import javasdk.exceptions.OpenFeatureError;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenFeatureClient implements Client {
    private final OpenFeatureAPI openfeatureApi;
    @Getter private String name;
    @Getter private String version;
    @Getter private List<Hook> clientHooks;

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
            mergedHooks = ImmutableList.<Hook>builder().addAll(options.getHooks()).addAll(clientHooks).build();
        } else {
            mergedHooks = clientHooks;
        }

        FlagEvaluationDetails<T> details = null;
        try {
            this.beforeHooks(hookCtx, mergedHooks);

            final ProviderEvaluation<T> providerEval;
            if (type == FlagValueType.BOOLEAN) {
                // TODO: Can we guarantee that defaultValue is a boolean? If not, how to we handle that?
                providerEval = (ProviderEvaluation<T>) provider.getBooleanEvaluation(key, (Boolean) defaultValue, ctx, options);
            } else {
                throw new GeneralError("Unknown flag type");
            }

            details = FlagEvaluationDetails.from(providerEval, key, null);
            this.afterHooks(hookCtx, details, mergedHooks);
        } catch (OpenFeatureError e) {
            // TODO: convert to error type if that's relevant.
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().value(defaultValue).reason(Reason.ERROR).build();
            }
            details.value = defaultValue;
            details.reason = Reason.ERROR;
            details.errorCode = e.getErrorCode();
            details.executedHooks = hookCtx.executedHooks;
        } finally {
            this.afterAllHooks(hookCtx, mergedHooks);
        }

        return details;
    }

    private void afterAllHooks(HookContext hookCtx, List<Hook> hooks) {
        for (Hook hook : hooks) {
            hookCtx.executedHooks.addAfterAll(hook);
            hook.afterAll(hookCtx);
        }
    }

    private <T> void afterHooks(HookContext hookContext, FlagEvaluationDetails<T> details, List<Hook> hooks) {
        for (Hook hook : hooks) {
            hookContext.executedHooks.addAfter(hook);
            hook.after(hookContext, details);
        }
    }

    private HookContext beforeHooks(HookContext hookCtx, List<Hook> hooks) {
        for (Hook hook : hooks) {
            hookCtx.executedHooks.addBefore(hook);
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
        return null;
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }

    @Override
    public Long getNumberValue(String key, Long defaultValue) {
        return null;
    }

    @Override
    public Long getNumberValue(String key, Long defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public Long getNumberValue(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }
}

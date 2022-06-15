package dev.openfeature.javasdk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import dev.openfeature.javasdk.exceptions.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    public void addHooks(Hook... hooks) {
        this.clientHooks.addAll(Arrays.asList(hooks));
    }

    <T> FlagEvaluationDetails<T> evaluateFlag(FlagValueType type, String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        FeatureProvider provider = this.openfeatureApi.getProvider();
        ImmutableMap<String, Object> hints = options.getHookHints();
        if (ctx == null) {
            ctx = new EvaluationContext();
        }

        // merge of: API.context, client.context, invocation.context

        // TODO: Context transformation?
        HookContext hookCtx = HookContext.from(key, type, this.getMetadata(), OpenFeatureAPI.getInstance().getProvider().getMetadata(), ctx, defaultValue);

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
            EvaluationContext ctxFromHook = this.beforeHooks(hookCtx, mergedHooks, hints);
            EvaluationContext invocationContext = EvaluationContext.merge(ctxFromHook, ctx);

            ProviderEvaluation<T> providerEval;
            if (type == FlagValueType.BOOLEAN) {
                // TODO: Can we guarantee that defaultValue is a boolean? If not, how to we handle that?
                providerEval = (ProviderEvaluation<T>) provider.getBooleanEvaluation(key, (Boolean) defaultValue, invocationContext, options);
            } else if(type == FlagValueType.STRING) {
                providerEval = (ProviderEvaluation<T>) provider.getStringEvaluation(key, (String) defaultValue, invocationContext, options);
            } else if (type == FlagValueType.INTEGER) {
                providerEval = (ProviderEvaluation<T>) provider.getIntegerEvaluation(key, (Integer) defaultValue, invocationContext, options);
            } else if (type == FlagValueType.OBJECT) {
                providerEval = (ProviderEvaluation<T>) provider.getObjectEvaluation(key, defaultValue, invocationContext, options);
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
            details.errorCode = e.getMessage();
            this.errorHooks(hookCtx, e, mergedHooks, hints);
        } finally {
            this.afterAllHooks(hookCtx, mergedHooks, hints);
        }

        return details;
    }

    // errorHooks and afterAllHooks error handling seems consistent with spec, and what I implemented. I'm glad we landed here.
    private void errorHooks(HookContext hookCtx, Exception e, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        for (Hook hook : hooks) {
            try {
                hook.error(hookCtx, e, hints);
            } catch (Exception inner_exception) {
                log.error("Exception when running error hooks " + hook.getClass().toString(), inner_exception);
            }
        }
    }

    private void afterAllHooks(HookContext hookCtx, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        for (Hook hook : hooks) {
            try {
                hook.finallyAfter(hookCtx, hints);
            } catch (Exception inner_exception) {
                log.error("Exception when running finally hooks " + hook.getClass().toString(), inner_exception);
            }
        }
    }

    private <T> void afterHooks(HookContext hookContext, FlagEvaluationDetails<T> details, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        for (Hook hook : hooks) {
            hook.after(hookContext, details, hints);
        }
    }

    private EvaluationContext beforeHooks(HookContext hookCtx, List<Hook> hooks, ImmutableMap<String, Object> hints) {
        // These traverse backwards from normal.
        EvaluationContext ctx = hookCtx.getCtx();
        // This is a bit counter-intuitive to me, just want to make sure... this is the stage that gets reversed because ArrayList.addAll() does a prepend, right?
        for (Hook hook : Lists.reverse(hooks)) {
            Optional<EvaluationContext> newCtx = hook.before(hookCtx, hints);
            if (newCtx != null && newCtx.isPresent()) {
                ctx = EvaluationContext.merge(ctx, newCtx.get());
                hookCtx = hookCtx.withCtx(ctx);
            }
        }
        return ctx;
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
        return getStringDetails(key, defaultValue,  null);
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
        return new Metadata() {
            @Override
            public String getName() {
                return name;
            }
        };
    }
}

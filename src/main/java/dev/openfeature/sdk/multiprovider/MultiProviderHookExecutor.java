package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ClientMetadata;
import dev.openfeature.sdk.DefaultHookData;
import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.FlagValueType;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.HookData;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Reason;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.ExceptionUtils;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs per-provider hook lifecycles during flag evaluation.
 *
 * <p>Mirrors the role of {@code HookExecutor} in the JS SDK: executes the before/after/error/finally
 * stages for each child provider's own hooks, using context captured by {@link MultiProvider}'s
 * provider-level hook.
 */
@Slf4j
class MultiProviderHookExecutor {

    private final ClientMetadata fallbackClientMetadata;

    MultiProviderHookExecutor(ClientMetadata fallbackClientMetadata) {
        this.fallbackClientMetadata = fallbackClientMetadata;
    }

    <T> ProviderEvaluation<T> evaluate(
            FeatureProvider provider,
            String key,
            T defaultValue,
            EvaluationContext ctx,
            HookExecutionContext hookExecutionContext,
            FlagValueType valueType,
            BiFunction<FeatureProvider, EvaluationContext, ProviderEvaluation<T>> providerFunction) {
        List<HookExecution<T>> hooks = supportedHooks(provider.getProviderHooks(), valueType);
        if (hooks.isEmpty()) {
            return providerFunction.apply(provider, ctx);
        }
        return new Lifecycle<T>(provider, key, defaultValue, valueType, hookExecutionContext, hooks)
                .run(ctx, providerFunction);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> List<HookExecution<T>> supportedHooks(List<Hook> rawHooks, FlagValueType valueType) {
        if (rawHooks == null || rawHooks.isEmpty()) {
            return Collections.emptyList();
        }
        List<HookExecution<T>> hooks = new ArrayList<>(rawHooks.size());
        for (Hook hook : rawHooks) {
            if (hook.supportsFlagValueType(valueType)) {
                hooks.add(new HookExecution<>(hook, new DefaultHookData()));
            }
        }
        return hooks;
    }

    /**
     * Runs the hook lifecycle for a single provider evaluation.
     *
     * <p>Holds the invariant evaluation parameters so that each stage can be executed by its own
     * method. Instances are single-use and confined to the calling thread.
     */
    private final class Lifecycle<T> {

        private final FeatureProvider provider;
        private final String key;
        private final T defaultValue;
        private final FlagValueType valueType;
        private final HookExecutionContext hookExecutionContext;
        private final Map<String, Object> hookHints;
        private final List<HookExecution<T>> hooks;
        private final List<HookExecution<T>> reversedHooks;
        private EvaluationContext evaluatedContext;
        private FlagEvaluationDetails<T> details;

        private Lifecycle(
                FeatureProvider provider,
                String key,
                T defaultValue,
                FlagValueType valueType,
                HookExecutionContext hookExecutionContext,
                List<HookExecution<T>> hooks) {
            this.provider = provider;
            this.key = key;
            this.defaultValue = defaultValue;
            this.valueType = valueType;
            this.hookExecutionContext = hookExecutionContext;
            this.hookHints = resolveHookHints(hookExecutionContext);
            this.hooks = hooks;
            // Per spec, before hooks run in registration order; after/error/finally run in reverse.
            this.reversedHooks = new ArrayList<>(hooks);
            Collections.reverse(this.reversedHooks);
        }

        private ProviderEvaluation<T> run(
                EvaluationContext ctx,
                BiFunction<FeatureProvider, EvaluationContext, ProviderEvaluation<T>> providerFunction) {
            evaluatedContext = copyEvaluationContext(ctx);
            try {
                runBefore();
                ProviderEvaluation<T> providerEvaluation =
                        providerFunction.apply(provider, toProviderContext(ctx, evaluatedContext));
                details = FlagEvaluationDetails.from(providerEvaluation, key);
                if (providerEvaluation.getErrorCode() == null) {
                    runAfter();
                } else {
                    enrichDetailsWithErrorDefaults(defaultValue, details);
                    runError(toEvaluationException(providerEvaluation));
                }
                return providerEvaluation;
            } catch (Exception e) {
                details = buildErrorDetails(key, defaultValue, details, e);
                runError(e);
                throw e;
            } finally {
                runFinallyAfter();
            }
        }

        private void runBefore() {
            for (HookExecution<T> execution : hooks) {
                HookContext<T> hookContext = hookContext(execution);
                var contextUpdate = execution.hook.before(hookContext, hookHints);
                // Hooks are invoked through a raw type; those predating Optional may return null.
                if (contextUpdate != null // NOSONAR
                        && contextUpdate.isPresent()
                        && contextUpdate.get() != hookContext.getCtx()
                        && !contextUpdate.get().isEmpty()) {
                    evaluatedContext = evaluatedContext.merge(contextUpdate.get());
                }
            }
        }

        private void runAfter() {
            for (HookExecution<T> execution : reversedHooks) {
                execution.hook.after(hookContext(execution), details, hookHints);
            }
        }

        private void runError(Exception error) {
            for (HookExecution<T> execution : reversedHooks) {
                try {
                    execution.hook.error(hookContext(execution), error, hookHints);
                } catch (Exception e) {
                    log.error("error executing provider hook error stage", e);
                }
            }
        }

        private void runFinallyAfter() {
            FlagEvaluationDetails<T> finalDetails = details == null
                    ? FlagEvaluationDetails.<T>builder()
                            .flagKey(key)
                            .value(defaultValue)
                            .build()
                    : details;
            for (HookExecution<T> execution : reversedHooks) {
                try {
                    execution.hook.finallyAfter(hookContext(execution), finalDetails, hookHints);
                } catch (Exception e) {
                    log.error("error executing provider hook finally stage", e);
                }
            }
        }

        private HookContext<T> hookContext(HookExecution<T> execution) {
            return createHookContext(
                    key, valueType, defaultValue, evaluatedContext, provider, hookExecutionContext, execution.hookData);
        }
    }

    private EvaluationContext copyEvaluationContext(EvaluationContext context) {
        if (context == null) {
            return ImmutableContext.EMPTY;
        }
        String targetingKey = context.getTargetingKey();
        if (targetingKey == null) {
            return new ImmutableContext(context.asMap());
        }
        return new ImmutableContext(targetingKey, context.asMap());
    }

    private EvaluationContext toProviderContext(EvaluationContext originalContext, EvaluationContext evaluatedContext) {
        if (originalContext == null && (evaluatedContext == null || evaluatedContext.isEmpty())) {
            return null;
        }
        return evaluatedContext;
    }

    private Exception toEvaluationException(ProviderEvaluation<?> providerEvaluation) {
        if (providerEvaluation == null || providerEvaluation.getErrorCode() == null) {
            return new RuntimeException("Provider evaluation returned an error");
        }
        return ExceptionUtils.instantiateErrorByErrorCode(
                providerEvaluation.getErrorCode(), providerEvaluation.getErrorMessage());
    }

    @SuppressWarnings("deprecation")
    private <T> HookContext<T> createHookContext(
            String key,
            FlagValueType valueType,
            T defaultValue,
            EvaluationContext evaluationContext,
            FeatureProvider provider,
            HookExecutionContext hookExecutionContext,
            HookData hookData) {
        return HookContext.<T>builder()
                .flagKey(key)
                .type(valueType)
                .defaultValue(normalizeDefaultValue(valueType, defaultValue))
                .ctx(evaluationContext)
                .clientMetadata(resolveClientMetadata(hookExecutionContext))
                .providerMetadata(provider.getMetadata())
                .hookData(hookData)
                .build();
    }

    /**
     * Returns a non-null default value for use in hook contexts when the caller passes {@code null}.
     * The returned object is always assignment-compatible with the expected type for {@code valueType}.
     */
    @SuppressWarnings("unchecked")
    private <T> T normalizeDefaultValue(FlagValueType valueType, T defaultValue) {
        if (defaultValue != null) {
            return defaultValue;
        }
        Object fallback;
        switch (valueType) {
            case BOOLEAN:
                fallback = Boolean.FALSE;
                break;
            case STRING:
                fallback = "";
                break;
            case INTEGER:
                fallback = Integer.valueOf(0);
                break;
            case DOUBLE:
                fallback = Double.valueOf(0d);
                break;
            case OBJECT:
                fallback = new Value();
                break;
            default:
                return defaultValue;
        }
        // Safe: the SDK guarantees T matches the valueType enum.
        return (T) fallback;
    }

    private ClientMetadata resolveClientMetadata(HookExecutionContext hookExecutionContext) {
        if (hookExecutionContext == null || hookExecutionContext.clientMetadata == null) {
            return fallbackClientMetadata;
        }
        return hookExecutionContext.clientMetadata;
    }

    private Map<String, Object> resolveHookHints(HookExecutionContext hookExecutionContext) {
        if (hookExecutionContext == null || hookExecutionContext.hints == null) {
            return Collections.emptyMap();
        }
        return hookExecutionContext.hints;
    }

    private <T> FlagEvaluationDetails<T> buildErrorDetails(
            String key, T defaultValue, FlagEvaluationDetails<T> details, Exception error) {
        FlagEvaluationDetails<T> errorDetails = details == null
                ? FlagEvaluationDetails.<T>builder().flagKey(key).build()
                : details;
        if (error instanceof OpenFeatureError) {
            errorDetails.setErrorCode(((OpenFeatureError) error).getErrorCode());
        } else {
            errorDetails.setErrorCode(ErrorCode.GENERAL);
        }
        errorDetails.setErrorMessage(error.getMessage());
        enrichDetailsWithErrorDefaults(defaultValue, errorDetails);
        return errorDetails;
    }

    private <T> void enrichDetailsWithErrorDefaults(T defaultValue, FlagEvaluationDetails<T> details) {
        details.setValue(defaultValue);
        details.setReason(Reason.ERROR.toString());
    }

    private static final class HookExecution<T> {
        private final Hook<T> hook;
        private final HookData hookData;

        private HookExecution(Hook<T> hook, HookData hookData) {
            this.hook = hook;
            this.hookData = hookData;
        }
    }
}

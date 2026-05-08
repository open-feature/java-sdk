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

    @SuppressWarnings({"rawtypes", "unchecked"})
    <T> ProviderEvaluation<T> evaluate(
            FeatureProvider provider,
            String key,
            T defaultValue,
            EvaluationContext ctx,
            HookExecutionContext hookExecutionContext,
            FlagValueType valueType,
            BiFunction<FeatureProvider, EvaluationContext, ProviderEvaluation<T>> providerFunction) {
        List<Hook> rawHooks = provider.getProviderHooks();
        if (rawHooks == null || rawHooks.isEmpty()) {
            return providerFunction.apply(provider, ctx);
        }

        List<HookExecution<T>> hooks = new ArrayList<>(rawHooks.size());
        for (Hook hook : rawHooks) {
            if (hook.supportsFlagValueType(valueType)) {
                hooks.add(new HookExecution<>(hook, new DefaultHookData()));
            }
        }

        if (hooks.isEmpty()) {
            return providerFunction.apply(provider, ctx);
        }

        EvaluationContext evaluatedContext = copyEvaluationContext(ctx);
        ProviderEvaluation<T> providerEvaluation = null;
        FlagEvaluationDetails<T> details = null;
        Map<String, Object> hookHints = resolveHookHints(hookExecutionContext);

        try {
            for (int i = hooks.size() - 1; i >= 0; i--) {
                HookExecution<T> execution = hooks.get(i);
                HookContext<T> hookContext = createHookContext(
                        key,
                        valueType,
                        defaultValue,
                        evaluatedContext,
                        provider,
                        hookExecutionContext,
                        execution.hookData);
                var contextUpdate = execution.hook.before(hookContext, hookHints);
                // Raw-type invocation: third-party hooks predating Optional may return null.
                if (contextUpdate != null // NOSONAR
                        && contextUpdate.isPresent()
                        && contextUpdate.get() != hookContext.getCtx()
                        && !contextUpdate.get().isEmpty()) {
                    evaluatedContext = evaluatedContext.merge(contextUpdate.get());
                }
            }

            providerEvaluation = providerFunction.apply(provider, toProviderContext(ctx, evaluatedContext));
            details = FlagEvaluationDetails.from(providerEvaluation, key);

            if (providerEvaluation.getErrorCode() == null) {
                for (HookExecution<T> execution : hooks) {
                    execution.hook.after(
                            createHookContext(
                                    key,
                                    valueType,
                                    defaultValue,
                                    evaluatedContext,
                                    provider,
                                    hookExecutionContext,
                                    execution.hookData),
                            details,
                            hookHints);
                }
            } else {
                enrichDetailsWithErrorDefaults(defaultValue, details);
                Exception providerException = toEvaluationException(providerEvaluation);
                for (HookExecution<T> execution : hooks) {
                    try {
                        execution.hook.error(
                                createHookContext(
                                        key,
                                        valueType,
                                        defaultValue,
                                        evaluatedContext,
                                        provider,
                                        hookExecutionContext,
                                        execution.hookData),
                                providerException,
                                hookHints);
                    } catch (Exception e) {
                        log.error("error executing provider hook error stage", e);
                    }
                }
            }

            return providerEvaluation;
        } catch (Exception e) {
            details = buildErrorDetails(key, defaultValue, details, e);
            for (HookExecution<T> execution : hooks) {
                try {
                    execution.hook.error(
                            createHookContext(
                                    key,
                                    valueType,
                                    defaultValue,
                                    evaluatedContext,
                                    provider,
                                    hookExecutionContext,
                                    execution.hookData),
                            e,
                            hookHints);
                } catch (Exception hookError) {
                    log.error("error executing provider hook error stage", hookError);
                }
            }
            throw e;
        } finally {
            FlagEvaluationDetails<T> finalDetails = details == null
                    ? FlagEvaluationDetails.<T>builder()
                            .flagKey(key)
                            .value(defaultValue)
                            .build()
                    : details;
            for (HookExecution<T> execution : hooks) {
                try {
                    execution.hook.finallyAfter(
                            createHookContext(
                                    key,
                                    valueType,
                                    defaultValue,
                                    evaluatedContext,
                                    provider,
                                    hookExecutionContext,
                                    execution.hookData),
                            finalDetails,
                            hookHints);
                } catch (Exception e) {
                    log.error("error executing provider hook finally stage", e);
                }
            }
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

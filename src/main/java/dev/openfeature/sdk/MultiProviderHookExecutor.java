package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.ExceptionUtils;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Runs a single provider's own hooks around one evaluation, delegating to {@link HookSupport} so a
 * child provider's hooks behave the same as when the provider is used standalone.
 *
 * <p><b>Internal.</b> Public only so {@link dev.openfeature.sdk.multiprovider.MultiProvider} can
 * reach it across packages; {@link HookSupport} and its collaborators are package-private. Not part
 * of the public API; may change or be removed without notice.
 *
 * @hidden
 */
public final class MultiProviderHookExecutor {

    private final HookSupport hookSupport = new HookSupport();

    /**
     * Runs {@code provider}'s hooks around the evaluation performed by {@code providerFunction}.
     */
    public <T> ProviderEvaluation<T> execute(
            FeatureProvider provider,
            String key,
            T defaultValue,
            FlagValueType type,
            EvaluationContext context,
            ClientMetadata clientMetadata,
            Map<String, Object> hints,
            BiFunction<FeatureProvider, EvaluationContext, ProviderEvaluation<T>> providerFunction) {

        List<Hook> providerHooks = provider.getProviderHooks();
        if (providerHooks == null || providerHooks.isEmpty()) {
            return providerFunction.apply(provider, context);
        }

        HookSupportData data = new HookSupportData();
        data.hints = hints == null ? Collections.emptyMap() : hints;
        LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, context);
        data.evaluationContext = layeredContext;

        hookSupport.setHooks(
                data, providerHooks, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), type);
        if (data.getHooks().isEmpty()) {
            // no hooks support this type
            return providerFunction.apply(provider, context);
        }

        SharedHookContext<T> sharedHookContext =
                new SharedHookContext<>(key, type, clientMetadata, provider.getMetadata(), defaultValue);
        hookSupport.setHookContexts(data, sharedHookContext, layeredContext);

        FlagEvaluationDetails<T> details = null;
        try {
            hookSupport.executeBeforeHooks(data);
            ProviderEvaluation<T> providerEvaluation = providerFunction.apply(provider, data.getEvaluationContext());
            details = FlagEvaluationDetails.from(providerEvaluation, key);
            if (details.getErrorCode() != null) {
                Exception error =
                        ExceptionUtils.instantiateErrorByErrorCode(details.getErrorCode(), details.getErrorMessage());
                enrichDetailsWithErrorDefaults(defaultValue, details);
                hookSupport.executeErrorHooks(data, error);
            } else {
                hookSupport.executeAfterHooks(data, details);
            }
            return providerEvaluation;
        } catch (Exception e) {
            if (details == null) {
                details = FlagEvaluationDetails.<T>builder().flagKey(key).build();
            }
            if (e instanceof OpenFeatureError) {
                details.setErrorCode(((OpenFeatureError) e).getErrorCode());
            } else {
                details.setErrorCode(ErrorCode.GENERAL);
            }
            details.setErrorMessage(e.getMessage());
            enrichDetailsWithErrorDefaults(defaultValue, details);
            hookSupport.executeErrorHooks(data, e);
            throw e;
        } finally {
            // details is always set by now: from the evaluation on success, or the catch on failure
            hookSupport.executeAfterAllHooks(data, details);
        }
    }

    private static <T> void enrichDetailsWithErrorDefaults(T defaultValue, FlagEvaluationDetails<T> details) {
        details.setValue(defaultValue);
        details.setReason(Reason.ERROR.toString());
    }
}

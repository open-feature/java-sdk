package dev.openfeature.api.lifecycle;

import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Hook;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.types.ClientMetadata;
import dev.openfeature.api.types.ProviderMetadata;

/**
 * A interface to hold immutable context that {@link Hook} instances use.
 */
public interface HookContext<T> {

    static <T> HookContext<T> of(
            final String flagKey,
            final T defaultValue,
            FlagValueType type,
            ProviderMetadata providerMetadata,
            ClientMetadata clientMetadata,
            EvaluationContext evaluationContext) {
        return new DefaultHookContext<>(
                flagKey, defaultValue, type, providerMetadata, clientMetadata, evaluationContext);
    }

    String getFlagKey();

    FlagValueType getType();

    T getDefaultValue();

    EvaluationContext getCtx();

    ClientMetadata getClientMetadata();

    ProviderMetadata getProviderMetadata();

    HookData getHookData();
}

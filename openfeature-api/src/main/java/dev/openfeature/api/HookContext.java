package dev.openfeature.api;

/**
 * A interface to hold immutable context that {@link Hook} instances use.
 */
public interface HookContext<T> {

    String getFlagKey();

    FlagValueType getType();

    T getDefaultValue();

    EvaluationContext getCtx();

    ClientMetadata getClientMetadata();

    ProviderMetadata getProviderMetadata();

    default HookData getHookData() {
        return null;
    }
}

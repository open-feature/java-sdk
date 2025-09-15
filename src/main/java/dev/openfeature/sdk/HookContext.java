package dev.openfeature.sdk;

import dev.openfeature.sdk.HookContextWithoutData.HookContextWithoutDataBuilder;

/**
 * A interface to hold immutable context that {@link Hook} instances use.
 */
public interface HookContext<T> {
    /**
     * Builds a {@link HookContextWithoutData} instances from request data.
     *
     * @param key              feature flag key
     * @param type             flag value type
     * @param clientMetadata   info on which client is calling
     * @param providerMetadata info on the provider
     * @param ctx              Evaluation Context for the request
     * @param defaultValue     Fallback value
     * @param <T>              type that the flag is evaluating against
     * @return resulting context for hook
     * @deprecated this should not be instantiated outside the SDK anymore
     */
    @Deprecated
    static <T> HookContext<T> from(
            String key,
            FlagValueType type,
            ClientMetadata clientMetadata,
            Metadata providerMetadata,
            EvaluationContext ctx,
            T defaultValue) {
        return new HookContextWithoutData<>(key, type, defaultValue, ctx, clientMetadata, providerMetadata);
    }

    /**
     * Returns a builder for our default HookContext object.
     */
    static <T> HookContextWithoutDataBuilder<T> builder() {
        return HookContextWithoutData.<T>builder();
    }

    String getFlagKey();

    FlagValueType getType();

    T getDefaultValue();

    EvaluationContext getCtx();

    ClientMetadata getClientMetadata();

    Metadata getProviderMetadata();

    default HookData getHookData() {
        return null;
    }
}

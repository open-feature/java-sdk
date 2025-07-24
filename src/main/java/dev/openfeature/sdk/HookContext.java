package dev.openfeature.sdk;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

/**
 * A data class to hold immutable context that {@link Hook} instances use.
 *
 * @param <T> the type for the flag being evaluated
 */
@Value
@Builder(toBuilder = true)
@With
public class HookContext<T> {
    @NonNull String flagKey;

    @NonNull FlagValueType type;

    @NonNull T defaultValue;

    @NonNull EvaluationContext ctx;

    ClientMetadata clientMetadata;
    Metadata providerMetadata;

    /**
     * Hook data provides a way for hooks to maintain state across their execution stages.
     * Each hook instance gets its own isolated data store.
     */
    HookData hookData;

    /**
     * Builds a {@link HookContext} instances from request data.
     *
     * @param key              feature flag key
     * @param type             flag value type
     * @param clientMetadata   info on which client is calling
     * @param providerMetadata info on the provider
     * @param ctx              Evaluation Context for the request
     * @param defaultValue     Fallback value
     * @param <T>              type that the flag is evaluating against
     * @return resulting context for hook
     */
    public static <T> HookContext<T> from(
            String key,
            FlagValueType type,
            ClientMetadata clientMetadata,
            Metadata providerMetadata,
            EvaluationContext ctx,
            T defaultValue) {
        return HookContext.<T>builder()
                .flagKey(key)
                .type(type)
                .clientMetadata(clientMetadata)
                .providerMetadata(providerMetadata)
                .ctx(ctx)
                .defaultValue(defaultValue)
                .hookData(null) // Explicitly set to null for backward compatibility
                .build();
    }
}

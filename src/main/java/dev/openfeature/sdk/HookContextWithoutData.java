package dev.openfeature.sdk;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.With;

/**
 * A data class to hold immutable context that {@link Hook} instances use.
 *
 * @param <T> the type for the flag being evaluated
 */
@Data
@Builder
@With
@Setter(AccessLevel.PRIVATE)
class HookContextWithoutData<T> implements HookContext<T> {
    @NonNull String flagKey;

    @NonNull FlagValueType type;

    @NonNull T defaultValue;

    @Setter(AccessLevel.PACKAGE)
    @NonNull EvaluationContext ctx;

    ClientMetadata clientMetadata;
    Metadata providerMetadata;

    /**
     * Builds a {@link HookContextWithoutData} instances from request data.
     *
     * @param key              feature flag key
     * @param type             flag value type
     * @param clientMetadata   info on which client is calling
     * @param providerMetadata info on the provider
     * @param defaultValue     Fallback value
     * @param <T>              type that the flag is evaluating against
     * @return resulting context for hook
     */
    static <T> HookContextWithoutData<T> from(
            String key, FlagValueType type, ClientMetadata clientMetadata, Metadata providerMetadata, T defaultValue) {
        return new HookContextWithoutData<>(
                key, type, defaultValue, ImmutableContext.EMPTY, clientMetadata, providerMetadata);
    }

    /**
     * Make the builder visible for javadocs.
     *
     * @param <T>   flag value type
     */
    public static class HookContextWithoutDataBuilder<T> {}
}

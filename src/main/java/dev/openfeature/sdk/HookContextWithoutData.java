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
@Builder
@With
class HookContextWithoutData<T> implements HookContext<T> {
    @NonNull String flagKey;

    @NonNull FlagValueType type;

    @NonNull T defaultValue;

    @NonNull EvaluationContext ctx;

    ClientMetadata clientMetadata;
    Metadata providerMetadata;
}

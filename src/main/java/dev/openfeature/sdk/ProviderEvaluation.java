package dev.openfeature.sdk;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains information about how the a flag was evaluated, including the resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderEvaluation<T> implements BaseEvaluation<T> {
    T value;
    @Nullable String variant;
    @Nullable private String reason;
    ErrorCode errorCode;
    @Nullable private String errorMessage;
    @Builder.Default
    private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();
}

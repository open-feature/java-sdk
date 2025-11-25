package dev.openfeature.sdk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains information about how the provider resolved a flag, including the
 * resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlagEvaluationDetails<T> implements BaseEvaluation<T> {

    private String flagKey;
    private T value;
    private String variant;
    private String reason;
    private ErrorCode errorCode;
    private String errorMessage;

    @Builder.Default
    private ImmutableMetadata flagMetadata = ImmutableMetadata.EMPTY;

    /**
     * Generate detail payload from the provider response.
     *
     * @param providerEval provider response
     * @param flagKey      key for the flag being evaluated
     * @param <T>          type of flag being returned
     * @return detail payload
     */
    public static <T> FlagEvaluationDetails<T> from(ProviderEvaluation<T> providerEval, String flagKey) {
        var flagMetadata = providerEval.getFlagMetadata();
        if (flagMetadata == null) {
            flagMetadata = ImmutableMetadata.EMPTY;
        }

        return new FlagEvaluationDetails<>(
                flagKey,
                providerEval.getValue(),
                providerEval.getVariant(),
                providerEval.getReason(),
                providerEval.getErrorCode(),
                providerEval.getErrorMessage(),
                flagMetadata);
    }
}

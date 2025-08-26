package dev.openfeature.api;

import java.util.Optional;
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
    private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();

    /**
     * Generate detail payload from the provider response.
     *
     * @param providerEval provider response
     * @param flagKey      key for the flag being evaluated
     * @param <T>          type of flag being returned
     * @return detail payload
     */
    public static <T> FlagEvaluationDetails<T> from(ProviderEvaluation<T> providerEval, String flagKey) {
        return FlagEvaluationDetails.<T>builder()
                .flagKey(flagKey)
                .value(providerEval.getValue())
                .variant(providerEval.getVariant())
                .reason(providerEval.getReason())
                .errorMessage(providerEval.getErrorMessage())
                .errorCode(providerEval.getErrorCode())
                .flagMetadata(Optional.ofNullable(providerEval.getFlagMetadata())
                        .orElse(ImmutableMetadata.builder().build()))
                .build();
    }
}

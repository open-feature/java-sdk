package dev.openfeature.sdk;

import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;

/**
 * Contains information about how the evaluation happened, including any resolved values.
 *
 * @param <T> the type of the flag being evaluated.
 */
@Data
@Builder
public class FlagEvaluationDetails<T> implements BaseEvaluation<T> {

    private String flagKey;
    private T value;
    @Nullable private String variant;
    @Nullable private String reason;
    private ErrorCode errorCode;
    @Nullable private String errorMessage;
    @Builder.Default private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();

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
                .errorCode(providerEval.getErrorCode())
                .flagMetadata(providerEval.getFlagMetadata())
                .build();
    }
}

package dev.openfeature.javasdk;

import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;

/**
 * Contains information about how the evaluation happened, including any resolved values.
 * @param <T> the type of the flag being evaluated.
 */
@Data @Builder
public class FlagEvaluationDetails<T> implements BaseEvaluation<T> {
    String flagKey;
    T value;
    @Nullable String variant;
    Reason reason;
    @Nullable String errorCode;

    public static <T> FlagEvaluationDetails<T> from(ProviderEvaluation<T> providerEval, String flagKey) {
        return FlagEvaluationDetails.<T>builder()
                .flagKey(flagKey)
                .value(providerEval.getValue())
                .variant(providerEval.getVariant())
                .reason(providerEval.getReason())
                .errorCode(providerEval.getErrorCode())
                .build();
    }
}

package javasdk;

import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;

@Data @Builder
public class FlagEvaluationDetails<T> implements BaseEvaluation<T> {
    String flagKey;
    HookEvaluation executedHooks;
    T value;
    @Nullable String variant;
    Reason reason;
    @Nullable ErrorCode errorCode;

    public static <T> FlagEvaluationDetails<T> from(ProviderEvaluation<T> providerEval, String flagKey, HookEvaluation executedHooks) {
        return FlagEvaluationDetails.<T>builder()
                .flagKey(flagKey)
                .executedHooks(executedHooks)
                .value(providerEval.getValue())
                .variant(providerEval.getVariant())
                .reason(providerEval.getReason())
                .errorCode(providerEval.getErrorCode())
                .build();
    }
}

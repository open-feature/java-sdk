package javasdk;

import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.util.List;

public class FlagEvaluationDetails<T> extends ProviderEvaluation {
    String flagKey;
    List<Hook> executedHooks;

    public FlagEvaluationDetails(T value, Reason reason) {
        super(value, reason);
    }

    public FlagEvaluationDetails(String flagKey, T value, Reason reason, @Nullable ErrorCode errorCode, List<Hook> executedHooks) {
        super(value, reason);
        this.errorCode = errorCode;
        this.flagKey = flagKey;
        this.executedHooks = executedHooks;
    }

    public static <T> FlagEvaluationDetails<T> from(ProviderEvaluation<T> providerEval, String flagKey, List<Hook> executedHooks) {
        return new FlagEvaluationDetails(flagKey, providerEval.value, providerEval.reason, providerEval.errorCode, executedHooks);
    }
}

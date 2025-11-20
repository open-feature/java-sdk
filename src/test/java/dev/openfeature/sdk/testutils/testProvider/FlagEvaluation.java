package dev.openfeature.sdk.testutils.testProvider;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FlagValueType;

public class FlagEvaluation {
    public final String flagKey;
    public final FlagValueType flagType;
    public final EvaluationContext evaluationContext;

    public FlagEvaluation(String flagKey, FlagValueType flagType, EvaluationContext evaluationContext) {
        this.flagKey = flagKey;
        this.flagType = flagType;
        this.evaluationContext = evaluationContext;
    }
}

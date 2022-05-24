package dev.openfeature.javasdk;

public class EvaluationContext {
    /**
     * Merges two EvaluationContext objects with the second overriding the first in case of conflict.
     */
    public static EvaluationContext merge(EvaluationContext ctx1, EvaluationContext ctx2) {
        // TODO(abrahms): Actually implement this when we know what the fields of EC are.
        return ctx1;
    }
}

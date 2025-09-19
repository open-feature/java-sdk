package dev.openfeature.api.evaluation;

/**
 * TBD.
 */
public interface EvaluationContextHolder<T> {
    /**
     * Return an optional client-level evaluation context.
     *
     * @return {@link EvaluationContext}
     */
    EvaluationContext getEvaluationContext();

    /**
     * Set the client-level evaluation context.
     *
     * @param ctx Client level context.
     */
    T setEvaluationContext(EvaluationContext ctx);
}

package dev.openfeature.api;

/**
 * Interface for evaluation context management.
 * Provides global context configuration that affects all flag evaluations.
 */
public interface OpenFeatureContext {
    /**
     * Sets the global evaluation context, which will be used for all evaluations.
     *
     * @param evaluationContext the context
     * @return api instance for method chaining
     */
    OpenFeatureAPI setEvaluationContext(EvaluationContext evaluationContext);

    /**
     * Gets the global evaluation context, which will be used for all evaluations.
     *
     * @return evaluation context
     */
    EvaluationContext getEvaluationContext();
}

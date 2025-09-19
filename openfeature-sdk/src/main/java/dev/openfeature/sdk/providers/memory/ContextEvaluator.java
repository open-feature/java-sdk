package dev.openfeature.sdk.providers.memory;

import dev.openfeature.api.evaluation.EvaluationContext;

/**
 * Context evaluator - use for resolving flag according to evaluation context, for handling targeting.
 *
 * @param <T> expected value type
 */
public interface ContextEvaluator<T> {

    T evaluate(Flag flag, EvaluationContext evaluationContext);
}

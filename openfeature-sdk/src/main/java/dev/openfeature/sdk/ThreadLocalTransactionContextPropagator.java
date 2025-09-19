package dev.openfeature.sdk;

import dev.openfeature.api.TransactionContextPropagator;
import dev.openfeature.api.evaluation.EvaluationContext;

/**
 * A {@link ThreadLocalTransactionContextPropagator} is a transactional context propagator
 * that uses a ThreadLocal to persist a transactional context for the duration of a single thread.
 *
 * @see TransactionContextPropagator
 */
public class ThreadLocalTransactionContextPropagator implements TransactionContextPropagator {

    private final ThreadLocal<EvaluationContext> evaluationContextThreadLocal = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public EvaluationContext getEvaluationContext() {
        return this.evaluationContextThreadLocal.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadLocalTransactionContextPropagator setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContextThreadLocal.set(evaluationContext);
        return this;
    }
}

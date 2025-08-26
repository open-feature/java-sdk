package dev.openfeature.sdk;

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
    public EvaluationContext getTransactionContext() {
        return this.evaluationContextThreadLocal.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransactionContext(EvaluationContext evaluationContext) {
        this.evaluationContextThreadLocal.set(evaluationContext);
    }
}

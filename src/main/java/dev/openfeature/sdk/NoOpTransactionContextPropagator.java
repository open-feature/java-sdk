package dev.openfeature.sdk;

/**
 * A {@link TransactionContextPropagator} that simply returns null.
 */
public class NoOpTransactionContextPropagator implements TransactionContextPropagator {

    /**
     * {@inheritDoc}
     * @return null
     */
    @Override
    public EvaluationContext getTransactionContext() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransactionContext(EvaluationContext evaluationContext) {

    }
}

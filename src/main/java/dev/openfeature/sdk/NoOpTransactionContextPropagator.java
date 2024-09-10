package dev.openfeature.sdk;

/**
 * A {@link TransactionContextPropagator} that simply returns empty context.
 */
public class NoOpTransactionContextPropagator implements TransactionContextPropagator {

    /**
     * {@inheritDoc}
     *
     * @return empty immutable context
     */
    @Override
    public EvaluationContext getTransactionContext() {
        return new ImmutableContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransactionContext(EvaluationContext evaluationContext) {

    }
}

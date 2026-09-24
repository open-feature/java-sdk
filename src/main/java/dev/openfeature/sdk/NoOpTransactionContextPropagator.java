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
        return ImmutableContext.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransactionContext(EvaluationContext evaluationContext) {}
}

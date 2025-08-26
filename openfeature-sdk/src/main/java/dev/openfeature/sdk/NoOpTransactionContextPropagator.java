package dev.openfeature.sdk;

import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.ImmutableContext;
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
    public void setTransactionContext(EvaluationContext evaluationContext) {}
}

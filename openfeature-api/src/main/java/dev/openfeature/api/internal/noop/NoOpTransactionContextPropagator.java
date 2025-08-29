package dev.openfeature.api.internal.noop;

import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.ImmutableContext;
import dev.openfeature.api.TransactionContextPropagator;
import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;

/**
 * A {@link TransactionContextPropagator} that simply returns empty context.
 *
 * <p><strong>This is an internal implementation class and should not be used directly by external users.</strong>
 */
@ExcludeFromGeneratedCoverageReport
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

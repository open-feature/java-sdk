package dev.openfeature.api.internal.noop;

import dev.openfeature.api.TransactionContextPropagator;
import dev.openfeature.api.evaluation.EvaluationContext;
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
    public EvaluationContext getEvaluationContext() {
        return EvaluationContext.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoOpTransactionContextPropagator setEvaluationContext(EvaluationContext evaluationContext) {
        return this;
    }
}

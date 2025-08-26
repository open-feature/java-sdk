package dev.openfeature.api;

/**
 * Interface for transaction context management operations.
 * Provides transaction-scoped context propagation and management,
 * allowing for context to be passed across multiple operations
 * within the same transaction or thread boundary.
 */
public interface OpenFeatureTransactionContext {
    /**
     * Return the transaction context propagator.
     *
     * @return the current transaction context propagator
     */
    TransactionContextPropagator getTransactionContextPropagator();

    /**
     * Sets the transaction context propagator.
     *
     * @param transactionContextPropagator the transaction context propagator to use
     * @throws IllegalArgumentException if {@code transactionContextPropagator} is null
     */
    void setTransactionContextPropagator(TransactionContextPropagator transactionContextPropagator);

    /**
     * Sets the transaction context using the registered transaction context propagator.
     *
     * @param evaluationContext the evaluation context to set for the current transaction
     */
    void setTransactionContext(EvaluationContext evaluationContext);
}

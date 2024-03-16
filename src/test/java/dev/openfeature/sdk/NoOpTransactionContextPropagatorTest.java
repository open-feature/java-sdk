package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoOpTransactionContextPropagatorTest {

    NoOpTransactionContextPropagator contextPropagator = new NoOpTransactionContextPropagator();

    @Test
    public void emptyTransactionContext() {
        EvaluationContext result = contextPropagator.getTransactionContext();
        assertNull(result);
    }

    @Test
    public void setTransactionContext() {
        EvaluationContext firstContext = new ImmutableContext();
        contextPropagator.setTransactionContext(firstContext);
        assertNull(contextPropagator.getTransactionContext());
    }
}
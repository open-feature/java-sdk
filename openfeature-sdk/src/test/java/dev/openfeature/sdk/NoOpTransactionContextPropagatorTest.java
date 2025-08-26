package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NoOpTransactionContextPropagatorTest {

    NoOpTransactionContextPropagator contextPropagator = new NoOpTransactionContextPropagator();

    @Test
    public void emptyTransactionContext() {
        EvaluationContext result = contextPropagator.getTransactionContext();
        assertTrue(result.asMap().isEmpty());
    }

    @Test
    public void setTransactionContext() {
        Map<String, Value> transactionAttrs = new HashMap<>();
        transactionAttrs.put("userId", new Value("userId"));
        EvaluationContext transactionCtx = new ImmutableContext(transactionAttrs);
        contextPropagator.setTransactionContext(transactionCtx);
        EvaluationContext result = contextPropagator.getTransactionContext();
        assertTrue(result.asMap().isEmpty());
    }
}

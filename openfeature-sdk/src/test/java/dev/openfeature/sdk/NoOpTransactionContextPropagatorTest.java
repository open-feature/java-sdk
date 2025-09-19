package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.internal.noop.NoOpTransactionContextPropagator;
import dev.openfeature.api.types.Value;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NoOpTransactionContextPropagatorTest {

    NoOpTransactionContextPropagator contextPropagator = new NoOpTransactionContextPropagator();

    @Test
    public void emptyTransactionContext() {
        EvaluationContext result = contextPropagator.getEvaluationContext();
        assertTrue(result.asMap().isEmpty());
    }

    @Test
    public void setEvaluationContext() {
        Map<String, Value> transactionAttrs = new HashMap<>();
        transactionAttrs.put("userId", new Value("userId"));
        EvaluationContext transactionCtx = EvaluationContext.immutableOf(transactionAttrs);
        contextPropagator.setEvaluationContext(transactionCtx);
        EvaluationContext result = contextPropagator.getEvaluationContext();
        assertTrue(result.asMap().isEmpty());
    }
}

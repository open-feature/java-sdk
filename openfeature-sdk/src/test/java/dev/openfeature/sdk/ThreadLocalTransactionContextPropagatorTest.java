package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.openfeature.api.evaluation.EvaluationContext;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.junit.jupiter.api.Test;

public class ThreadLocalTransactionContextPropagatorTest {

    ThreadLocalTransactionContextPropagator contextPropagator = new ThreadLocalTransactionContextPropagator();

    @Test
    public void setEvaluationContextOneThread() {
        EvaluationContext firstContext = EvaluationContext.EMPTY;
        contextPropagator.setEvaluationContext(firstContext);
        assertSame(firstContext, contextPropagator.getEvaluationContext());
        EvaluationContext secondContext = EvaluationContext.immutableOf(new HashMap<>());
        contextPropagator.setEvaluationContext(secondContext);
        assertNotSame(firstContext, contextPropagator.getEvaluationContext());
        assertSame(secondContext, contextPropagator.getEvaluationContext());
    }

    @Test
    public void emptyTransactionContext() {
        EvaluationContext result = contextPropagator.getEvaluationContext();
        assertNull(result);
    }

    @Test
    public void setEvaluationContextTwoThreads() throws Exception {
        EvaluationContext firstContext = EvaluationContext.EMPTY;
        EvaluationContext secondContext = EvaluationContext.EMPTY;

        Callable<EvaluationContext> callable = () -> {
            assertNull(contextPropagator.getEvaluationContext());
            contextPropagator.setEvaluationContext(secondContext);
            EvaluationContext transactionContext = contextPropagator.getEvaluationContext();
            assertSame(secondContext, transactionContext);
            return transactionContext;
        };
        contextPropagator.setEvaluationContext(firstContext);
        EvaluationContext firstThreadContext = contextPropagator.getEvaluationContext();
        assertSame(firstContext, firstThreadContext);

        FutureTask<EvaluationContext> futureTask = new FutureTask<>(callable);
        Thread thread = new Thread(futureTask);
        thread.start();
        EvaluationContext secondThreadContext = futureTask.get();

        assertSame(secondContext, secondThreadContext);
        assertSame(firstContext, contextPropagator.getEvaluationContext());
    }
}

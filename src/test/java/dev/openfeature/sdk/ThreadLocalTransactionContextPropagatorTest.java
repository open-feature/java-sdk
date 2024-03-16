package dev.openfeature.sdk;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadLocalTransactionContextPropagatorTest {

    ThreadLocalTransactionContextPropagator contextPropagator = new ThreadLocalTransactionContextPropagator();

    @Test
    public void setTransactionContextOneThread() {
        EvaluationContext firstContext = new ImmutableContext();
        contextPropagator.setTransactionContext(firstContext);
        assertSame(firstContext, contextPropagator.getTransactionContext());
        EvaluationContext secondContext = new ImmutableContext();
        contextPropagator.setTransactionContext(secondContext);
        assertNotSame(firstContext, contextPropagator.getTransactionContext());
        assertSame(secondContext, contextPropagator.getTransactionContext());
    }

    @Test
    public void emptyTransactionContext() {
        EvaluationContext result = contextPropagator.getTransactionContext();
        assertNull(result);
    }

    @SneakyThrows
    @Test
    public void setTransactionContextTwoThreads() {
        EvaluationContext firstContext = new ImmutableContext();
        EvaluationContext secondContext = new ImmutableContext();

        Callable<EvaluationContext> callable = () -> {
            assertNull(contextPropagator.getTransactionContext());
            contextPropagator.setTransactionContext(secondContext);
            EvaluationContext transactionContext = contextPropagator.getTransactionContext();
            assertSame(secondContext, transactionContext);
            return transactionContext;
        };
        contextPropagator.setTransactionContext(firstContext);
        EvaluationContext firstThreadContext = contextPropagator.getTransactionContext();
        assertSame(firstContext, firstThreadContext);

        FutureTask<EvaluationContext> futureTask = new FutureTask<>(callable);
        Thread thread = new Thread(futureTask);
        thread.start();
        EvaluationContext secondThreadContext = futureTask.get();

        assertSame(secondContext, secondThreadContext);
        assertSame(firstContext, firstThreadContext);
    }
}
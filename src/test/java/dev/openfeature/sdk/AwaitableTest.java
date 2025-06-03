package dev.openfeature.sdk;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class AwaitableTest {
    @Test
    void waitingForFinishedIsANoOp() {
        var startTime = System.currentTimeMillis();
        Awaitable.FINISHED.await();
        var endTime = System.currentTimeMillis();
        assertTrue(endTime - startTime < 10);
    }

    @Test
    void waitingForNotFinishedWaitsEvenWhenInterrupted() throws InterruptedException {
        var awaitable = new Awaitable();
        var mayProceed = new AtomicBoolean(false);

        var thread = new Thread(() -> {
            awaitable.await();
            if (!mayProceed.get()) {
                fail();
            }
        });
        thread.start();

        var startTime = System.currentTimeMillis();
        do {
            thread.interrupt();
        } while (startTime + 1000 > System.currentTimeMillis());
        mayProceed.set(true);
        awaitable.wakeup();
        thread.join();
    }

    @Test
    void callingWakeUpWakesUpAllWaitingThreads() throws InterruptedException {
        var awaitable = new Awaitable();
        var isRunning = new AtomicInteger();

        Runnable runnable = () -> {
            isRunning.incrementAndGet();
            var start = System.currentTimeMillis();
            awaitable.await();
            var end = System.currentTimeMillis();
            if (end - start > 10) {
                fail();
            }
        };

        var numThreads = 2;
        var threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(runnable);
            threads[i].start();
        }

        await().atMost(1, TimeUnit.SECONDS).until(() -> isRunning.get() == numThreads);

        awaitable.wakeup();

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
    }
}

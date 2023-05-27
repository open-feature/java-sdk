package dev.openfeature.sdk.testutils.stubbing;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import lombok.experimental.UtilityClass;
import org.mockito.stubbing.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.doAnswer;

@UtilityClass
public class ConditionStubber {

    @SuppressWarnings("java:S2925")
    public static Stubber doDelayResponse(Duration duration) {
        return doAnswer(invocation -> {
            MILLISECONDS.sleep(duration.toMillis());
            return null;
        });
    }

    public static Stubber doBlock(CountDownLatch latch) {
        return doAnswer(invocation -> {
            latch.await();
            return null;
        });
    }

    public static <T> Stubber doBlock(CountDownLatch latch, Answer<T> answer) {
        return doAnswer(invocation -> {
            latch.await();
            return answer.answer(invocation);
        });
    }

}

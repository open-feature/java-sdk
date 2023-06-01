package dev.openfeature.sdk.fixtures;

import dev.openfeature.sdk.FeatureProvider;
import lombok.experimental.UtilityClass;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;

import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doBlock;
import static org.mockito.Mockito.*;

@UtilityClass
public class ProviderFixture {

    public static FeatureProvider createMockedProvider() {
        return mock(FeatureProvider.class);
    }

    public static FeatureProvider createBlockedProvider(CountDownLatch latch, Runnable onAnswer) {
        FeatureProvider provider = createMockedProvider();
        doBlock(latch, createAnswerExecutingCode(onAnswer)).when(provider).initialize();
        doReturn("blockedProvider").when(provider).toString();
        return provider;
    }

    private static Answer<?> createAnswerExecutingCode(Runnable onAnswer) {
        return invocation -> {
            onAnswer.run();
            return null;
        };
    }

    public static FeatureProvider createUnblockingProvider(CountDownLatch latch) {
        FeatureProvider provider = createMockedProvider();
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(provider).initialize();
        doReturn("unblockingProvider").when(provider).toString();
        return provider;
    }

}

package dev.openfeature.sdk.fixtures;

import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.ProviderState;
import lombok.experimental.UtilityClass;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;

import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doBlock;
import static org.mockito.Mockito.*;

@UtilityClass
public class ProviderFixture {

    public static FeatureProvider createMockedProvider() {
        FeatureProvider provider = mock(FeatureProvider.class);
        doReturn(ProviderState.NOT_READY).when(provider).getState();
        return provider;
    }

    public static FeatureProvider createBlockedProvider(CountDownLatch latch, Runnable onAnswer) throws Exception {
        FeatureProvider provider = createMockedProvider();
        doBlock(latch, createAnswerExecutingCode(onAnswer)).when(provider).initialize(new ImmutableContext());
        doReturn("blockedProvider").when(provider).toString();
        return provider;
    }

    private static Answer<?> createAnswerExecutingCode(Runnable onAnswer) {
        return invocation -> {
            onAnswer.run();
            return null;
        };
    }

    public static FeatureProvider createUnblockingProvider(CountDownLatch latch) throws Exception {
        FeatureProvider provider = createMockedProvider();
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(provider).initialize(new ImmutableContext());
        doReturn("unblockingProvider").when(provider).toString();
        return provider;
    }

}

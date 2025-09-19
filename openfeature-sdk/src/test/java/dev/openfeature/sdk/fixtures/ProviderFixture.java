package dev.openfeature.sdk.fixtures;

import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doBlock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import dev.openfeature.api.Provider;
import dev.openfeature.api.evaluation.EvaluationContext;
import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import org.mockito.stubbing.Answer;

public class ProviderFixture {

    private ProviderFixture() {
        // Utility class
    }

    public static Provider createMockedProvider() {
        Provider provider = mock(Provider.class);

        // TODO: handle missing getState()
        // doReturn(ProviderState.NOT_READY).when(provider).getState();
        return provider;
    }

    public static Provider createMockedReadyProvider() {
        Provider provider = mock(Provider.class);

        // TODO: handle missing getState()
        // doReturn(ProviderState.READY).when(provider).getState();
        return provider;
    }

    public static Provider createMockedErrorProvider() throws Exception {
        Provider provider = mock(Provider.class);

        // TODO: handle missing getState()
        // doReturn(ProviderState.NOT_READY).when(provider).getState();
        doThrow(FileNotFoundException.class).when(provider).initialize(any());
        return provider;
    }

    public static Provider createBlockedProvider(CountDownLatch latch, Runnable onAnswer) throws Exception {
        Provider provider = createMockedProvider();
        doBlock(latch, createAnswerExecutingCode(onAnswer)).when(provider).initialize(EvaluationContext.EMPTY);
        doReturn("blockedProvider").when(provider).toString();
        return provider;
    }

    private static Answer<?> createAnswerExecutingCode(Runnable onAnswer) {
        return invocation -> {
            onAnswer.run();
            return null;
        };
    }

    public static Provider createUnblockingProvider(CountDownLatch latch) throws Exception {
        Provider provider = createMockedProvider();
        doAnswer(invocation -> {
                    latch.countDown();
                    return null;
                })
                .when(provider)
                .initialize(EvaluationContext.EMPTY);
        doReturn("unblockingProvider").when(provider).toString();
        return provider;
    }
}

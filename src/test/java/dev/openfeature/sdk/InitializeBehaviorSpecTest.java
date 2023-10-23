package dev.openfeature.sdk;

import dev.openfeature.sdk.testutils.exception.TestException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class InitializeBehaviorSpecTest {

    @BeforeEach
    void setupTest() {
        OpenFeatureAPI.getInstance().setProvider(new NoOpProvider());
    }

    @Nested
    class DefaultProvider {

        @Specification(number = "1.1.2.2", text = "The `provider mutator` function MUST invoke the `initialize` "
            + "function on the newly registered provider before using it to resolve flag values.")
        @Test
        @DisplayName("must call initialize function of the newly registered provider before using it for "
            + "flag evaluation")
        void mustCallInitializeFunctionOfTheNewlyRegisteredProviderBeforeUsingItForFlagEvaluation() throws Exception {
            FeatureProvider featureProvider = mock(FeatureProvider.class);
            doReturn(ProviderState.NOT_READY).when(featureProvider).getState();

            OpenFeatureAPI.getInstance().setProvider(featureProvider);

            verify(featureProvider, timeout(1000)).initialize(any());
        }

        @Specification(number = "1.4.10", text = "Methods, functions, or operations on the client MUST NOT throw "
            + "exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the "
            + "`default value` in the event of abnormal execution. Exceptions include functions or methods for "
            + "the purposes for configuration or setup.")
        @Test
        @DisplayName("should catch exception thrown by the provider on initialization")
        void shouldCatchExceptionThrownByTheProviderOnInitialization() throws Exception {
            FeatureProvider featureProvider = mock(FeatureProvider.class);
            doReturn(ProviderState.NOT_READY).when(featureProvider).getState();
            doThrow(TestException.class).when(featureProvider).initialize(any());

            assertThatCode(() -> OpenFeatureAPI.getInstance().setProvider(featureProvider))
                .doesNotThrowAnyException();

            verify(featureProvider, timeout(1000)).initialize(any());
        }
    }

    @Nested
    class ProviderForNamedClient {

        @Specification(number = "1.1.2.2", text = "The `provider mutator` function MUST invoke the `initialize`"
            + " function on the newly registered provider before using it to resolve flag values.")
        @Test
        @DisplayName("must call initialize function of the newly registered named provider before using it "
            + "for flag evaluation")
        void mustCallInitializeFunctionOfTheNewlyRegisteredNamedProviderBeforeUsingItForFlagEvaluation() throws Exception {
            FeatureProvider featureProvider = mock(FeatureProvider.class);
            doReturn(ProviderState.NOT_READY).when(featureProvider).getState();

            OpenFeatureAPI.getInstance().setProvider("clientName", featureProvider);

            verify(featureProvider, timeout(1000)).initialize(any());
        }

        @Specification(number = "1.4.10", text = "Methods, functions, or operations on the client MUST NOT throw "
            + "exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the "
            + "`default value` in the event of abnormal execution. Exceptions include functions or methods for "
            + "the purposes for configuration or setup.")
        @Test
        @DisplayName("should catch exception thrown by the named client provider on initialization")
        void shouldCatchExceptionThrownByTheNamedClientProviderOnInitialization() throws Exception {
            FeatureProvider featureProvider = mock(FeatureProvider.class);
            doReturn(ProviderState.NOT_READY).when(featureProvider).getState();
            doThrow(TestException.class).when(featureProvider).initialize(any());

            assertThatCode(() -> OpenFeatureAPI.getInstance().setProvider("clientName", featureProvider))
                .doesNotThrowAnyException();

            verify(featureProvider, timeout(1000)).initialize(any());
        }
    }
}

package dev.openfeature.sdk;

import dev.openfeature.sdk.testutils.exception.TestException;
import org.junit.jupiter.api.*;

import static dev.openfeature.sdk.testutils.FeatureProviderTestUtils.setFeatureProvider;
import static org.mockito.Mockito.*;

class ShutdownBehaviorSpecTest {

    @BeforeEach
    void resetFeatureProvider() {
        setFeatureProvider(new NoOpProvider());
    }

    @Nested
    class DefaultProvider {

        @Specification(number = "1.1.2.3", text = "The `provider mutator` function MUST invoke the `shutdown` function on the previously registered provider once it's no longer being used to resolve flag values.")
        @Test
        @DisplayName("must invoke shutdown method on previously registered provider once it should not be used for flag evaluation anymore")
        void mustInvokeShutdownMethodOnPreviouslyRegisteredProviderOnceItShouldNotBeUsedForFlagEvaluationAnymore() {
            FeatureProvider featureProvider = mock(FeatureProvider.class);

            setFeatureProvider(featureProvider);
            setFeatureProvider(new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }

        @Specification(number = "1.4.9", text = "Methods, functions, or operations on the client MUST NOT throw "
            + "exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the "
            + "`default value` in the event of abnormal execution. Exceptions include functions or methods for "
            + "the purposes for configuration or setup.")
        @Test
        @DisplayName("should catch exception thrown by the provider on shutdown")
        void shouldCatchExceptionThrownByTheProviderOnShutdown() {
            FeatureProvider featureProvider = mock(FeatureProvider.class);
            doThrow(TestException.class).when(featureProvider).shutdown();

            setFeatureProvider(featureProvider);
            setFeatureProvider(new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }
    }

    @Nested
    class NamedProvider {

        @Specification(number = "1.1.2.3", text = "The `provider mutator` function MUST invoke the `shutdown` function on the previously registered provider once it's no longer being used to resolve flag values.")
        @Test
        @DisplayName("must invoke shutdown method on previously registered provider once it should not be used for flag evaluation anymore")
        void mustInvokeShutdownMethodOnPreviouslyRegisteredProviderOnceItShouldNotBeUsedForFlagEvaluationAnymore() {
            String clientName = "clientName";
            FeatureProvider featureProvider = mock(FeatureProvider.class);

            setFeatureProvider(clientName, featureProvider);
            setFeatureProvider(clientName, new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }

        @Specification(number = "1.4.9", text = "Methods, functions, or operations on the client MUST NOT throw "
            + "exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the "
            + "`default value` in the event of abnormal execution. Exceptions include functions or methods for "
            + "the purposes for configuration or setup.")
        @Test
        @DisplayName("should catch exception thrown by the named client provider on shutdown")
        void shouldCatchExceptionThrownByTheNamedClientProviderOnShutdown() {
            String clientName = "clientName";
            FeatureProvider featureProvider = mock(FeatureProvider.class);
            doThrow(TestException.class).when(featureProvider).shutdown();

            setFeatureProvider(clientName, featureProvider);
            setFeatureProvider(clientName, new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }

    }
}

package dev.openfeature.sdk;

import static dev.openfeature.sdk.testutils.FeatureProviderTestUtils.setFeatureProvider;
import static org.mockito.Mockito.*;

import dev.openfeature.sdk.fixtures.ProviderFixture;
import dev.openfeature.sdk.testutils.exception.TestException;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ShutdownBehaviorSpecTest {

    private String DOMAIN = "myDomain";

    @BeforeEach
    void resetFeatureProvider() {
        setFeatureProvider(new NoOpProvider());
    }

    @Nested
    class DefaultProvider {

        @Specification(
                number = "1.1.2.3",
                text =
                        "The `provider mutator` function MUST invoke the `shutdown` function on the previously registered provider once it's no longer being used to resolve flag values.")
        @Test
        @DisplayName(
                "must invoke shutdown method on previously registered provider once it should not be used for flag evaluation anymore")
        void mustInvokeShutdownMethodOnPreviouslyRegisteredProviderOnceItShouldNotBeUsedForFlagEvaluationAnymore() {
            FeatureProvider featureProvider = ProviderFixture.createMockedProvider();

            setFeatureProvider(featureProvider);
            setFeatureProvider(new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }

        @Specification(
                number = "1.4.10",
                text = "Methods, functions, or operations on the client MUST NOT throw "
                        + "exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the "
                        + "`default value` in the event of abnormal execution. Exceptions include functions or methods for "
                        + "the purposes for configuration or setup.")
        @Test
        @DisplayName("should catch exception thrown by the provider on shutdown")
        void shouldCatchExceptionThrownByTheProviderOnShutdown() {
            FeatureProvider featureProvider = ProviderFixture.createMockedProvider();
            doThrow(TestException.class).when(featureProvider).shutdown();

            setFeatureProvider(featureProvider);
            setFeatureProvider(new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }
    }

    @Nested
    class NamedProvider {

        @Specification(
                number = "1.1.2.3",
                text =
                        "The `provider mutator` function MUST invoke the `shutdown` function on the previously registered provider once it's no longer being used to resolve flag values.")
        @Test
        @DisplayName(
                "must invoke shutdown method on previously registered provider once it should not be used for flag evaluation anymore")
        void mustInvokeShutdownMethodOnPreviouslyRegisteredProviderOnceItShouldNotBeUsedForFlagEvaluationAnymore() {
            FeatureProvider featureProvider = ProviderFixture.createMockedProvider();

            setFeatureProvider(DOMAIN, featureProvider);
            setFeatureProvider(DOMAIN, new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }

        @Specification(
                number = "1.4.10",
                text = "Methods, functions, or operations on the client MUST NOT throw "
                        + "exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the "
                        + "`default value` in the event of abnormal execution. Exceptions include functions or methods for "
                        + "the purposes for configuration or setup.")
        @Test
        @DisplayName("should catch exception thrown by the named client provider on shutdown")
        void shouldCatchExceptionThrownByTheNamedClientProviderOnShutdown() {
            FeatureProvider featureProvider = ProviderFixture.createMockedProvider();
            doThrow(TestException.class).when(featureProvider).shutdown();

            setFeatureProvider(DOMAIN, featureProvider);
            setFeatureProvider(DOMAIN, new NoOpProvider());

            verify(featureProvider, timeout(1000)).shutdown();
        }
    }

    @Nested
    class General {

        @Specification(
                number = "1.6.1",
                text = "The API MUST define a mechanism to propagate a shutdown request to active providers.")
        @Test
        @DisplayName("must shutdown all providers on shutting down api")
        void mustShutdownAllProvidersOnShuttingDownApi() {
            FeatureProvider defaultProvider = ProviderFixture.createMockedProvider();
            FeatureProvider namedProvider = ProviderFixture.createMockedProvider();
            setFeatureProvider(defaultProvider);
            setFeatureProvider(DOMAIN, namedProvider);
            OpenFeatureAPI api = OpenFeatureAPI.getInstance();

            synchronized (OpenFeatureAPI.class) {
                api.shutdown();

                Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
                    verify(defaultProvider).shutdown();
                    verify(namedProvider).shutdown();
                });
            }
        }

        @Test
        @DisplayName("once shutdown is complete, api must be ready to use again")
        void apiIsReadyToUseAfterShutdown() {
            final OpenFeatureAPI openFeatureAPI = OpenFeatureAPI.getInstance();

            NoOpProvider p1 = new NoOpProvider();
            openFeatureAPI.setProvider(p1);

            openFeatureAPI.shutdown();

            NoOpProvider p2 = new NoOpProvider();
            openFeatureAPI.setProvider(p2);
        }
    }
}

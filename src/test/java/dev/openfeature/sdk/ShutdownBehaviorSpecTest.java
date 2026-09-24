package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private OpenFeatureAPI api;

    void setFeatureProvider(FeatureProvider featureProvider) {
        api.setProviderAndWait(featureProvider);
    }

    void setFeatureProvider(String domain, FeatureProvider featureProvider) {
        api.setProviderAndWait(domain, featureProvider);
    }

    @BeforeEach
    void resetFeatureProvider() {
        api = new OpenFeatureAPI();
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

            synchronized (OpenFeatureAPI.class) {
                api.shutdown();

                Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
                    verify(defaultProvider).shutdown();
                    verify(namedProvider).shutdown();
                });
            }
        }

        @Test
        @Specification(
                number = "1.6.2",
                text =
                        "The API's `shutdown` function MUST reset all state of the API, removing all hooks, event handlers, evaluation context, transaction context propagators, and providers.")
        @DisplayName("shutdown must reset the state of the API")
        void apiStateMustBeResetOnShuttingDownApi() {

            FeatureProvider provider = ProviderFixture.createMockedProvider();
            TransactionContextPropagator transactionContextPropagator = mock(TransactionContextPropagator.class);
            EvaluationContext evaluationContext = mock(EvaluationContext.class);

            api.addHooks(mock(Hook.class));
            api.setProvider(provider);
            api.setEvaluationContext(evaluationContext);
            api.setTransactionContextPropagator(transactionContextPropagator);

            api.shutdown();

            assertNotEquals(provider, api.getProvider());
            assertTrue(api.getHooks().isEmpty());
            assertNotEquals(evaluationContext, api.getEvaluationContext());
            assertNotEquals(transactionContextPropagator, api.getTransactionContextPropagator());

            assertInstanceOf(NoOpProvider.class, api.getProvider());
            assertInstanceOf(ImmutableContext.class, api.getEvaluationContext());
            assertInstanceOf(NoOpTransactionContextPropagator.class, api.getTransactionContextPropagator());
        }

        @Test
        @DisplayName("calling shutdown twice should be safe and idempotent")
        void callingShutdownTwiceShouldBeSafe() {
            FeatureProvider provider = ProviderFixture.createMockedProvider();
            setFeatureProvider(provider);

            api.shutdown();

            // Second shutdown should be a no-op (no exception, provider not called twice)
            api.shutdown();

            verify(provider, times(1)).shutdown();
        }
    }
}

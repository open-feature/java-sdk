package dev.openfeature.sdk;

import static dev.openfeature.sdk.fixtures.ProviderFixture.*;
import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doDelayResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.testutils.exception.TestException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProviderRepositoryTest {

    private static final String DOMAIN_NAME = "domain name";
    private static final String ANOTHER_DOMAIN_NAME = "another domain name";
    private static final int TIMEOUT = 5000;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private ProviderRepository providerRepository;

    @BeforeEach
    void setupTest() {
        providerRepository = new ProviderRepository(new OpenFeatureAPI());
    }

    @Nested
    class InitializationBehavior {

        @Nested
        class DefaultProvider {

            @Test
            @DisplayName("should reject null as default provider")
            void shouldRejectNullAsDefaultProvider() {
                assertThatCode(() -> providerRepository.setProvider(
                                null, mockAfterSet(), mockAfterInit(), mockAfterShutdown(), mockAfterError(), false))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("should have NoOpProvider set as default on initialization")
            void shouldHaveNoOpProviderSetAsDefaultOnInitialization() {
                assertThat(providerRepository.getProvider()).isInstanceOf(NoOpProvider.class);
            }

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() throws Exception {
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).initialize(new ImmutableContext());

                await().alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(
                                    featureProvider,
                                    mockAfterSet(),
                                    mockAfterInit(),
                                    mockAfterShutdown(),
                                    mockAfterError(),
                                    false);
                            verify(featureProvider, timeout(TIMEOUT)).initialize(any());
                            return true;
                        });

                verify(featureProvider, timeout(TIMEOUT)).initialize(any());
            }
        }

        @Nested
        class NamedProvider {

            @Test
            @DisplayName("should reject null as named provider")
            void shouldRejectNullAsNamedProvider() {
                assertThatCode(() -> providerRepository.setProvider(
                                DOMAIN_NAME,
                                null,
                                mockAfterSet(),
                                mockAfterInit(),
                                mockAfterShutdown(),
                                mockAfterError(),
                                false))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("should reject null as domain name")
            void shouldRejectNullAsDefaultProvider() {
                NoOpProvider provider = new NoOpProvider();
                assertThatCode(() -> providerRepository.setProvider(
                                null,
                                provider,
                                mockAfterSet(),
                                mockAfterInit(),
                                mockAfterShutdown(),
                                mockAfterError(),
                                false))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("should immediately return when calling the domain provider mutator")
            void shouldImmediatelyReturnWhenCallingTheDomainProviderMutator() throws Exception {
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).initialize(any());

                await().alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(
                                    "a domain",
                                    featureProvider,
                                    mockAfterSet(),
                                    mockAfterInit(),
                                    mockAfterShutdown(),
                                    mockAfterError(),
                                    false);
                            verify(featureProvider, timeout(TIMEOUT)).initialize(any());
                            return true;
                        });
            }
        }
    }

    @Nested
    class ShutdownBehavior {

        @Nested
        class DefaultProvider {

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() throws Exception {
                FeatureProvider newProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(newProvider).initialize(any());

                await().alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(
                                    newProvider,
                                    mockAfterSet(),
                                    mockAfterInit(),
                                    mockAfterShutdown(),
                                    mockAfterError(),
                                    false);
                            verify(newProvider, timeout(TIMEOUT)).initialize(any());
                            return true;
                        });

                verify(newProvider, timeout(TIMEOUT)).initialize(any());
            }

            @Test
            @DisplayName("should not call shutdown if replaced default provider is bound as named provider")
            void shouldNotCallShutdownIfReplacedDefaultProviderIsBoundAsNamedProvider() {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                setFeatureProvider(oldProvider);
                setFeatureProvider(DOMAIN_NAME, oldProvider);

                setFeatureProvider(newProvider);

                verify(oldProvider, never()).shutdown();
            }
        }

        @Nested
        class NamedProvider {

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() throws Exception {
                FeatureProvider newProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(newProvider).initialize(any());

                Future<?> providerMutation = executorService.submit(() -> providerRepository.setProvider(
                        DOMAIN_NAME,
                        newProvider,
                        mockAfterSet(),
                        mockAfterInit(),
                        mockAfterShutdown(),
                        mockAfterError(),
                        false));

                await().alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(providerMutation::isDone);
            }

            @Test
            @DisplayName("should not call shutdown if replaced provider is bound to multiple names")
            void shouldNotCallShutdownIfReplacedProviderIsBoundToMultipleNames() throws InterruptedException {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                setFeatureProvider(DOMAIN_NAME, oldProvider);

                setFeatureProvider(ANOTHER_DOMAIN_NAME, oldProvider);

                setFeatureProvider(DOMAIN_NAME, newProvider);

                verify(oldProvider, never()).shutdown();
            }

            @Test
            @DisplayName("should not call shutdown if replaced provider is bound as default provider")
            void shouldNotCallShutdownIfReplacedProviderIsBoundAsDefaultProvider() {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                setFeatureProvider(oldProvider);
                setFeatureProvider(DOMAIN_NAME, oldProvider);

                setFeatureProvider(DOMAIN_NAME, newProvider);

                verify(oldProvider, never()).shutdown();
            }

            @Test
            @DisplayName("should not throw exception if provider throws one on shutdown")
            void shouldNotThrowExceptionIfProviderThrowsOneOnShutdown() {
                FeatureProvider provider = createMockedProvider();
                doThrow(TestException.class).when(provider).shutdown();
                setFeatureProvider(provider);

                assertThatCode(() -> setFeatureProvider(new NoOpProvider())).doesNotThrowAnyException();

                verify(provider, timeout(TIMEOUT)).shutdown();
            }
        }

        @Nested
        class LifecyleLambdas {
            @Test
            @DisplayName("should run afterSet, afterInit, afterShutdown on successful set/init")
            @SuppressWarnings("unchecked")
            void shouldRunLambdasOnSuccessful() {
                Consumer<FeatureProvider> afterSet = mock(Consumer.class);
                Consumer<FeatureProvider> afterInit = mock(Consumer.class);
                Consumer<FeatureProvider> afterShutdown = mock(Consumer.class);
                BiConsumer<FeatureProvider, OpenFeatureError> afterError = mock(BiConsumer.class);

                FeatureProvider oldProvider = providerRepository.getProvider();
                FeatureProvider featureProvider1 = createMockedProvider();
                FeatureProvider featureProvider2 = createMockedProvider();

                setFeatureProvider(featureProvider1, afterSet, afterInit, afterShutdown, afterError);
                setFeatureProvider(featureProvider2);
                verify(afterSet, timeout(TIMEOUT)).accept(featureProvider1);
                verify(afterInit, timeout(TIMEOUT)).accept(featureProvider1);
                verify(afterShutdown, timeout(TIMEOUT)).accept(oldProvider);
                verify(afterError, never()).accept(any(), any());
            }

            @Test
            @DisplayName("should run afterSet, afterError on unsuccessful set/init")
            @SuppressWarnings("unchecked")
            void shouldRunLambdasOnError() throws Exception {
                Consumer<FeatureProvider> afterSet = mock(Consumer.class);
                Consumer<FeatureProvider> afterInit = mock(Consumer.class);
                Consumer<FeatureProvider> afterShutdown = mock(Consumer.class);
                BiConsumer<FeatureProvider, OpenFeatureError> afterError = mock(BiConsumer.class);

                FeatureProvider errorFeatureProvider = createMockedErrorProvider();

                setFeatureProvider(errorFeatureProvider, afterSet, afterInit, afterShutdown, afterError);
                verify(afterSet, timeout(TIMEOUT)).accept(errorFeatureProvider);
                verify(afterInit, never()).accept(any());
                ;
                verify(afterError, timeout(TIMEOUT)).accept(eq(errorFeatureProvider), any());
            }
        }

        @Nested
        class GracefulShutdownBehavior {

            @Test
            @DisplayName("should complete shutdown successfully when executor terminates within timeout")
            void shouldCompleteShutdownSuccessfullyWhenExecutorTerminatesWithinTimeout() {
                FeatureProvider provider = createMockedProvider();
                setFeatureProvider(provider);

                assertThatCode(() -> providerRepository.shutdown()).doesNotThrowAnyException();

                verify(provider, timeout(TIMEOUT)).shutdown();
            }

            @Test
            @DisplayName("should force shutdown when executor does not terminate within timeout")
            void shouldForceShutdownWhenExecutorDoesNotTerminateWithinTimeout() throws Exception {
                FeatureProvider provider = createMockedProvider();
                AtomicBoolean wasInterrupted = new AtomicBoolean(false);
                doAnswer(invocation -> {
                            try {
                                Thread.sleep(TIMEOUT);
                            } catch (InterruptedException e) {
                                wasInterrupted.set(true);
                                throw e;
                            }
                            return null;
                        })
                        .when(provider)
                        .shutdown();

                setFeatureProvider(provider);

                assertThatCode(() -> providerRepository.shutdown()).doesNotThrowAnyException();

                verify(provider, timeout(TIMEOUT)).shutdown();
                // Verify that shutdownNow() interrupted the running shutdown task
                await().atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(wasInterrupted.get()).isTrue());
            }

            // Note: shouldHandleInterruptionDuringShutdownGracefully was removed because the
            // interrupt timing is not guaranteed. Proper concurrency testing is done in
            // ProviderRepositoryCT using VMLens.

            @Test
            @DisplayName("should not hang indefinitely on shutdown")
            void shouldNotHangIndefinitelyOnShutdown() {
                FeatureProvider provider = createMockedProvider();
                setFeatureProvider(provider);

                await().alias("shutdown should complete within reasonable time")
                        .atMost(Duration.ofSeconds(5))
                        .until(() -> {
                            providerRepository.shutdown();
                            return true;
                        });
            }

            @Test
            @DisplayName("should handle shutdown during provider initialization")
            void shouldHandleShutdownDuringProviderInitialization() throws Exception {
                FeatureProvider slowInitProvider = createMockedProvider();
                AtomicBoolean shutdownCalled = new AtomicBoolean(false);

                doDelayResponse(Duration.ofMillis(500)).when(slowInitProvider).initialize(any());

                doAnswer(invocation -> {
                            shutdownCalled.set(true);
                            return null;
                        })
                        .when(slowInitProvider)
                        .shutdown();

                providerRepository.setProvider(
                        slowInitProvider,
                        mockAfterSet(),
                        mockAfterInit(),
                        mockAfterShutdown(),
                        mockAfterError(),
                        false);

                // Call shutdown while initialization is in progress
                assertThatCode(() -> providerRepository.shutdown()).doesNotThrowAnyException();

                await().atMost(Duration.ofSeconds(1)).untilTrue(shutdownCalled);
                verify(slowInitProvider, times(1)).shutdown();
            }

            @Test
            @DisplayName("should handle provider replacement during shutdown")
            void shouldHandleProviderReplacementDuringShutdown() throws Exception {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                AtomicBoolean oldProviderShutdownCalled = new AtomicBoolean(false);

                doAnswer(invocation -> {
                            oldProviderShutdownCalled.set(true);
                            return null;
                        })
                        .when(oldProvider)
                        .shutdown();

                providerRepository.setProvider(
                        oldProvider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(), mockAfterError(), true);

                // Replace provider (this will trigger old provider shutdown in background)
                providerRepository.setProvider(
                        newProvider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(), mockAfterError(), false);

                assertThatCode(() -> providerRepository.shutdown()).doesNotThrowAnyException();

                await().atMost(Duration.ofSeconds(1)).untilTrue(oldProviderShutdownCalled);
                verify(oldProvider, times(1)).shutdown();
                verify(newProvider, times(1)).shutdown();
            }

            @Test
            @DisplayName("should prevent adding providers after shutdown has started")
            void shouldPreventAddingProvidersAfterShutdownHasStarted() {
                FeatureProvider provider = createMockedProvider();
                setFeatureProvider(provider);

                providerRepository.shutdown();

                FeatureProvider newProvider = createMockedProvider();
                assertThatThrownBy(() -> setFeatureProvider(newProvider))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("shutting down");
            }

            @Test
            @DisplayName("prepareShutdown should return null on second call")
            void prepareShutdownShouldReturnNullOnSecondCall() {
                FeatureProvider provider = createMockedProvider();
                setFeatureProvider(provider);

                // First call should return managers list
                var managers = providerRepository.prepareShutdown();
                assertThat(managers).isNotNull();
                assertThat(managers).isNotEmpty();

                // Second call should be a no-op and return null (already shutting down)
                var secondResult = providerRepository.prepareShutdown();
                assertThat(secondResult).isNull();
            }

            @Test
            @DisplayName("should fall back to direct shutdown when executor rejects tasks")
            void shouldFallBackToDirectShutdownWhenExecutorRejectsTasks() throws Exception {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                AtomicBoolean initializationStarted = new AtomicBoolean(false);
                AtomicBoolean proceedWithInit = new AtomicBoolean(false);

                // Make oldProvider's initialization block until we signal
                doAnswer(invocation -> {
                            initializationStarted.set(true);
                            while (!proceedWithInit.get()) {
                                Thread.sleep(10);
                            }
                            return null;
                        })
                        .when(oldProvider)
                        .initialize(any());

                // Start async initialization (will block)
                providerRepository.setProvider(
                        oldProvider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(), mockAfterError(), false);

                // Wait for initialization to start
                await().atMost(Duration.ofSeconds(1)).untilTrue(initializationStarted);

                // Now set a new provider - this will trigger shutDownOld for oldProvider
                // after initialization completes, but we haven't completed init yet
                providerRepository.setProvider(
                        newProvider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(), mockAfterError(), false);

                // Call shutdown on repository - this will shutdown the executor
                var managers = providerRepository.prepareShutdown();
                providerRepository.completeShutdown(managers);

                // Now let the initialization complete - shutDownOld will be called but executor is shutdown
                // This triggers the RejectedExecutionException path which falls back to direct shutdown
                proceedWithInit.set(true);

                // Both providers should eventually be shut down (oldProvider via direct call due to
                // RejectedExecutionException)
                verify(oldProvider, timeout(TIMEOUT)).shutdown();
                verify(newProvider, timeout(TIMEOUT)).shutdown();
            }
        }
    }

    @Test
    @DisplayName("should shutdown all feature providers on shutdown")
    void shouldShutdownAllFeatureProvidersOnShutdown() {
        FeatureProvider featureProvider1 = createMockedProvider();
        FeatureProvider featureProvider2 = createMockedProvider();

        setFeatureProvider(featureProvider1);
        setFeatureProvider(DOMAIN_NAME, featureProvider1);
        setFeatureProvider(ANOTHER_DOMAIN_NAME, featureProvider2);

        providerRepository.shutdown();
        verify(featureProvider1, timeout(TIMEOUT)).shutdown();
        verify(featureProvider2, timeout(TIMEOUT)).shutdown();
    }

    private void setFeatureProvider(FeatureProvider provider) {
        providerRepository.setProvider(
                provider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(), mockAfterError(), false);
        waitForSettingProviderHasBeenCompleted(ProviderRepository::getProvider, provider);
    }

    private void setFeatureProvider(
            FeatureProvider provider,
            Consumer<FeatureProvider> afterSet,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, OpenFeatureError> afterError) {
        providerRepository.setProvider(provider, afterSet, afterInit, afterShutdown, afterError, false);
        waitForSettingProviderHasBeenCompleted(ProviderRepository::getProvider, provider);
    }

    private void setFeatureProvider(String namedProvider, FeatureProvider provider) {
        providerRepository.setProvider(
                namedProvider, provider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(), mockAfterError(), false);
        waitForSettingProviderHasBeenCompleted(repository -> repository.getProvider(namedProvider), provider);
    }

    private void waitForSettingProviderHasBeenCompleted(
            Function<ProviderRepository, FeatureProvider> extractor, FeatureProvider provider) {
        await().pollDelay(Duration.ofMillis(1)).atMost(Duration.ofSeconds(5)).until(() -> {
            return extractor.apply(providerRepository).equals(provider);
        });
    }

    private Consumer<FeatureProvider> mockAfterSet() {
        return fp -> {};
    }

    private Consumer<FeatureProvider> mockAfterInit() {
        return fp -> {};
    }

    private Consumer<FeatureProvider> mockAfterShutdown() {
        return fp -> {};
    }

    private BiConsumer<FeatureProvider, OpenFeatureError> mockAfterError() {
        return (fp, ex) -> {};
    }
}

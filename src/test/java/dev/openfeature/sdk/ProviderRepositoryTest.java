package dev.openfeature.sdk;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import static dev.openfeature.sdk.fixtures.ProviderFixture.*;
import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doBlock;
import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doDelayResponse;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProviderRepositoryTest {

    private static final String CLIENT_NAME = "client name";
    private static final String ANOTHER_CLIENT_NAME = "another client name";
    private static final String FEATURE_KEY = "some key";

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private ProviderRepository providerRepository;

    @BeforeEach
    void setupTest() {
        providerRepository = new ProviderRepository();
    }

    @Nested
    class InitializationBehavior {

        @Nested
        class DefaultProvider {

            @Test
            @DisplayName("should have NoOpProvider set as default on initialization")
            void shouldHaveNoOpProviderSetAsDefaultOnInitialization() {
                assertThat(providerRepository.getProvider()).isInstanceOf(NoOpProvider.class);
            }

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() {
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).initialize();

                await()
                        .alias("wait for provider mutator to return")
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(featureProvider);
                            verify(featureProvider, timeout(100)).initialize();
                            return true;
                        });

                verify(featureProvider).initialize();
            }

            @Test
            @DisplayName("should not return set provider if initialize has not yet been finished executing")
            void shouldNotReturnSetProviderIfItsInitializeMethodHasNotYetBeenFinishedExecuting() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider newProvider = createMockedProvider();
                doBlock(latch).when(newProvider).initialize();
                FeatureProvider oldProvider = providerRepository.getProvider();

                providerRepository.setProvider(newProvider);

                FeatureProvider providerWhileInitialization = providerRepository.getProvider();
                latch.countDown();

                assertThat(providerWhileInitialization).isEqualTo(oldProvider);
                await()
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(providerRepository.getProvider()).isEqualTo(newProvider));
                verify(newProvider, timeout(100)).initialize();
            }

            @SneakyThrows
            @Test
            @DisplayName("should discard provider still initializing if a newer has finished before")
            void shouldDiscardProviderStillInitializingIfANewerHasFinishedBefore() {
                CountDownLatch latch = new CountDownLatch(1);
                CountDownLatch testBlockingLatch = new CountDownLatch(1);
                FeatureProvider blockedProvider = createBlockedProvider(latch, () -> {
                    System.out.println("and down it goes...");
                    testBlockingLatch.countDown();
                });
                FeatureProvider fastProvider = createUnblockingProvider(latch);

                providerRepository.setProvider(blockedProvider);
                providerRepository.setProvider(fastProvider);

                assertThat(testBlockingLatch.await(2, SECONDS))
                        .as("blocking provider initialization not completed within 2 seconds")
                        .isTrue();

                await()
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(providerRepository.getProvider()).isEqualTo(fastProvider));

                verify(blockedProvider, timeout(100)).initialize();
                verify(fastProvider, timeout(100)).initialize();
            }
        }

        @Nested
        class NamedProvider {

            @Test
            @DisplayName("should immediately return when calling the named client provider mutator")
            void shouldImmediatelyReturnWhenCallingTheNamedClientProviderMutator() {
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).initialize();

                await()
                        .alias("wait for provider mutator to return")
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider("named client", featureProvider);
                            verify(featureProvider, timeout(1000)).initialize();
                            return true;
                        });
            }

            @Test
            @DisplayName("should not return set provider if it's initialization has not yet been finished executing")
            void shouldNotReturnSetProviderIfItsInitializeMethodHasNotYetBeenFinishedExecuting() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider newProvider = createMockedProvider();
                doBlock(latch).when(newProvider).initialize();
                FeatureProvider oldProvider = createMockedProvider();
                setFeatureProvider(CLIENT_NAME, oldProvider);

                providerRepository.setProvider(CLIENT_NAME, newProvider);
                FeatureProvider providerWhileInitialization = getNamedProvider();
                latch.countDown();

                assertThat(providerWhileInitialization).isEqualTo(oldProvider);
                await()
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(getNamedProvider()).isEqualTo(newProvider));
                verify(newProvider, timeout(100)).initialize();
            }

            @SneakyThrows
            @Test
            @DisplayName("should discard provider still initializing if a newer has finished before")
            void shouldDiscardProviderStillInitializingIfANewerHasFinishedBefore() {
                String clientName = "clientName";
                CountDownLatch latch = new CountDownLatch(1);
                CountDownLatch testBlockingLatch = new CountDownLatch(1);
                FeatureProvider blockedProvider = createBlockedProvider(latch, () -> {
                    System.out.println("and down it goes...");
                    testBlockingLatch.countDown();
                });
                FeatureProvider unblockingProvider = createUnblockingProvider(latch);

                providerRepository.setProvider(clientName, blockedProvider);
                providerRepository.setProvider(clientName, unblockingProvider);

                assertThat(testBlockingLatch.await(2, SECONDS))
                        .as("blocking provider initialization not completed within 2 seconds")
                        .isTrue();

                await()
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(providerRepository.getProvider(clientName))
                                .isEqualTo(unblockingProvider));

                verify(blockedProvider, timeout(100)).initialize();
                verify(unblockingProvider, timeout(100)).initialize();
            }
        }
    }

    @Nested
    class ShutdownBehavior {

        @Nested
        class DefaultProvider {

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() {
                FeatureProvider newProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(newProvider).initialize();

                await()
                        .alias("wait for provider mutator to return")
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(newProvider);
                            verify(newProvider, timeout(100)).initialize();
                            return true;
                        });

                verify(newProvider).initialize();
            }

            @Test
            @DisplayName("should use old provider if replacing one has not yet been finished initializing")
            void shouldUseOldProviderIfReplacingOneHasNotYetBeenFinishedInitializing() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider newProvider = createMockedProvider();
                doBlock(latch).when(newProvider).initialize();
                FeatureProvider oldProvider = createMockedProvider();

                setFeatureProvider(oldProvider);
                providerRepository.setProvider(newProvider);

                providerRepository.getProvider().getBooleanEvaluation("some key", true, null);
                latch.countDown();

                await()
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(getProvider()).isEqualTo(newProvider));
                verify(oldProvider, timeout(100)).getBooleanEvaluation(any(), any(), any());
                verify(newProvider, never()).getBooleanEvaluation(any(), any(), any());
            }

            @Test
            @DisplayName("should not call shutdown if replaced default provider is bound as named provider")
            void shouldNotCallShutdownIfReplacedDefaultProviderIsBoundAsNamedProvider() {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                setFeatureProvider(oldProvider);
                setFeatureProvider(CLIENT_NAME, oldProvider);

                setFeatureProvider(newProvider);

                verify(oldProvider, never()).shutdown();
            }
        }

        @Nested
        class NamedProvider {

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() {
                FeatureProvider newProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(newProvider).initialize();

                Future<?> providerMutation = executorService
                        .submit(() -> providerRepository.setProvider(CLIENT_NAME, newProvider));

                await()
                        .alias("wait for provider mutator to return")
                        .atMost(Duration.ofSeconds(1))
                        .until(providerMutation::isDone);
            }

            @Test
            @DisplayName("should use old provider if replacement one has not yet been finished initializing")
            void shouldUseOldProviderIfReplacementHasNotYetBeenFinishedInitializing() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider newProvider = createMockedProvider();
                doBlock(latch).when(newProvider).initialize();
                FeatureProvider oldProvider = createMockedProvider();

                setFeatureProvider(CLIENT_NAME, oldProvider);
                providerRepository.setProvider(CLIENT_NAME, newProvider);

                providerRepository.getProvider(CLIENT_NAME).getBooleanEvaluation(FEATURE_KEY, true, null);
                latch.countDown();

                await()
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(getNamedProvider()).isEqualTo(newProvider));
                verify(oldProvider, timeout(100)).getBooleanEvaluation(eq(FEATURE_KEY), any(), any());
                verify(newProvider, never()).getBooleanEvaluation(any(), any(), any());
            }

            @Test
            @DisplayName("should not call shutdown if replaced provider is bound to multiple names")
            void shouldNotCallShutdownIfReplacedProviderIsBoundToMultipleNames() {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                setFeatureProvider(CLIENT_NAME, oldProvider);
                setFeatureProvider(ANOTHER_CLIENT_NAME, oldProvider);

                setFeatureProvider(CLIENT_NAME, newProvider);

                verify(oldProvider, never()).shutdown();
            }

            @Test
            @DisplayName("should not call shutdown if replaced provider is bound as default provider")
            void shouldNotCallShutdownIfReplacedProviderIsBoundAsDefaultProvider() {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                setFeatureProvider(oldProvider);
                setFeatureProvider(CLIENT_NAME, oldProvider);

                setFeatureProvider(CLIENT_NAME, newProvider);

                verify(oldProvider, never()).shutdown();
            }
        }
    }

    @Test
    @DisplayName("should shutdown all feature providers on shutdown")
    void shouldShutdownAllFeatureProvidersOnShutdown() {
        FeatureProvider featureProvider1 = createMockedProvider();
        FeatureProvider featureProvider2 = createMockedProvider();

        setFeatureProvider(featureProvider1);
        setFeatureProvider(CLIENT_NAME, featureProvider1);
        setFeatureProvider(ANOTHER_CLIENT_NAME, featureProvider2);

        providerRepository.shutdown();

        await()
                .atMost(Duration.ofSeconds(1))
                .until(() -> {
                    verify(featureProvider1).shutdown();
                    verify(featureProvider2).shutdown();
                    return true;
                });
    }

    private FeatureProvider getProvider() {
        return providerRepository.getProvider();
    }

    private FeatureProvider getNamedProvider() {
        return providerRepository.getProvider(CLIENT_NAME);
    }

    private void setFeatureProvider(FeatureProvider provider) {
        providerRepository.setProvider(provider);
        waitForProviderInitializationComplete(ProviderRepository::getProvider, provider);
    }

    private void setFeatureProvider(String namedProvider, FeatureProvider provider) {
        providerRepository.setProvider(namedProvider, provider);
        waitForProviderInitializationComplete(repository -> repository.getProvider(namedProvider), provider);
    }

    private void waitForProviderInitializationComplete(
            Function<ProviderRepository, FeatureProvider> extractor,
            FeatureProvider provider) {
        await()
                .pollDelay(Duration.ofMillis(1))
                .atMost(Duration.ofSeconds(1))
                .until(() -> extractor.apply(providerRepository) == provider);
    }

}

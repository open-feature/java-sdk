package dev.openfeature.sdk;

import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doBlock;
import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doDelayResponse;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

class OpenFeatureAPITest {

    private static final String FEATURE_KEY = "some key";
    private static final String CLIENT_NAME = "client name";
    private static final String ANOTHER_CLIENT_NAME = "another client name";

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private OpenFeatureAPI api;

    @BeforeEach
    void setupTest() {
        api = OpenFeatureAPI.getInstance();
    }

    @Test
    void namedProviderTest() {
        FeatureProvider provider = new NoOpProvider();
        FeatureProviderTestUtils.setFeatureProvider("namedProviderTest", provider);

        assertThat(provider.getMetadata().getName())
                .isEqualTo(api.getProviderMetadata("namedProviderTest").getName());
    }

    @Test
    void settingDefaultProviderToNullErrors() {
        assertThatCode(() -> api.setProvider(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingNamedClientProviderToNullErrors() {
        assertThatCode(() -> api.setProvider(CLIENT_NAME, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    class InitializationBehavior {

        @Nested
        class DefaultProvider {

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() {
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).initialize();

                await()
                        .alias("wait for provider mutator to return")
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            api.setProvider(featureProvider);
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
                FeatureProvider oldProvider = api.getProvider();

                api.setProvider(newProvider);

                FeatureProvider providerWhileInitialization = api.getProvider();
                latch.countDown();

                assertThat(providerWhileInitialization).isEqualTo(oldProvider);
                await()
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(api.getProvider()).isEqualTo(newProvider));
                verify(newProvider, timeout(100)).initialize();
            }

            @SneakyThrows
            @Test
            @DisplayName("should discard provider still initializing if a newer has finished before")
            void shouldDiscardProviderStillInitializingIfANewerHasFinishedBefore() {
                CountDownLatch latch = new CountDownLatch(1);
                CountDownLatch testBlockingLatch = new CountDownLatch(1);
                FeatureProvider blockedProvider = blockedProvider(latch, () -> {
                    System.out.println("and down it goes...");
                    testBlockingLatch.countDown();
                });
                FeatureProvider fastProvider = unblockingProvider(latch);

                api.setProvider(blockedProvider);
                api.setProvider(fastProvider);

                assertThat(testBlockingLatch.await(2, SECONDS))
                        .as("blocking provider initialization not completed within 2 seconds")
                        .isTrue();

                await()
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(api.getProvider()).isEqualTo(fastProvider));

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
                            OpenFeatureAPI.getInstance().setProvider("named client", featureProvider);
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
                FeatureProviderTestUtils.setFeatureProvider(CLIENT_NAME, oldProvider);

                OpenFeatureAPI.getInstance().setProvider(CLIENT_NAME, newProvider);
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
                FeatureProvider blockedProvider = blockedProvider(latch, () -> {
                    System.out.println("and down it goes...");
                    testBlockingLatch.countDown();
                });
                FeatureProvider unblockingProvider = unblockingProvider(latch);

                api.setProvider(clientName, blockedProvider);
                api.setProvider(clientName, unblockingProvider);

                assertThat(testBlockingLatch.await(2, SECONDS))
                        .as("blocking provider initialization not completed within 2 seconds")
                        .isTrue();

                await()
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(api.getProvider(clientName)).isEqualTo(unblockingProvider));

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
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).shutdown();
                FeatureProviderTestUtils.setFeatureProvider(featureProvider);

                await()
                        .alias("wait for provider mutator to return")
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            api.setProvider(new NoOpProvider());
                            verify(featureProvider, timeout(100)).shutdown();
                            return true;
                        });

                verify(featureProvider).shutdown();
            }

            @Test
            @DisplayName("should use old provider if replacing one has not yet been finished initializing")
            void shouldUseOldProviderIfReplacingOneHasNotYetBeenFinishedInitializing() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider newProvider = createMockedProvider();
                doBlock(latch).when(newProvider).initialize();
                FeatureProvider oldProvider = createMockedProvider();

                FeatureProviderTestUtils.setFeatureProvider(oldProvider);
                OpenFeatureAPI.getInstance().setProvider(newProvider);

                OpenFeatureAPI.getInstance().getClient().getBooleanValue("some key", true);
                latch.countDown();

                await()
                        .atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(OpenFeatureAPITest.getProvider()).isEqualTo(newProvider));
                verify(oldProvider, timeout(100)).getBooleanEvaluation(any(), any(), any());
                verify(newProvider, never()).getBooleanEvaluation(any(), any(), any());
            }

            @Test
            @DisplayName("should not call shutdown if replaced default provider is bound as named provider")
            void shouldNotCallShutdownIfReplacedDefaultProviderIsBoundAsNamedProvider() {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                FeatureProviderTestUtils.setFeatureProvider(oldProvider);
                FeatureProviderTestUtils.setFeatureProvider(CLIENT_NAME, oldProvider);

                FeatureProviderTestUtils.setFeatureProvider(newProvider);

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

                Future<?> providerMutation = executorService.submit(() -> api.setProvider(CLIENT_NAME, newProvider));

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

                FeatureProviderTestUtils.setFeatureProvider(CLIENT_NAME, oldProvider);
                OpenFeatureAPI.getInstance().setProvider(CLIENT_NAME, newProvider);

                OpenFeatureAPI.getInstance().getClient(CLIENT_NAME).getBooleanValue(FEATURE_KEY, true);
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
                FeatureProviderTestUtils.setFeatureProvider(CLIENT_NAME, oldProvider);
                FeatureProviderTestUtils.setFeatureProvider(ANOTHER_CLIENT_NAME, oldProvider);

                FeatureProviderTestUtils.setFeatureProvider(CLIENT_NAME, newProvider);

                verify(oldProvider, never()).shutdown();
            }

            @Test
            @DisplayName("should not call shutdown if replaced provider is bound as default provider")
            void shouldNotCallShutdownIfReplacedProviderIsBoundAsDefaultProvider() {
                FeatureProvider oldProvider = createMockedProvider();
                FeatureProvider newProvider = createMockedProvider();
                FeatureProviderTestUtils.setFeatureProvider(oldProvider);
                FeatureProviderTestUtils.setFeatureProvider(CLIENT_NAME, oldProvider);

                FeatureProviderTestUtils.setFeatureProvider(CLIENT_NAME, newProvider);

                verify(oldProvider, never()).shutdown();
            }
        }
    }

    private static FeatureProvider createMockedProvider() {
        return mock(FeatureProvider.class);
    }

    private static FeatureProvider getProvider() {
        return OpenFeatureAPI.getInstance().getProvider();
    }

    private static FeatureProvider getNamedProvider() {
        return OpenFeatureAPI.getInstance().getProvider(OpenFeatureAPITest.CLIENT_NAME);
    }

    private FeatureProvider blockedProvider(CountDownLatch latch, Runnable onAnswer) {
        FeatureProvider provider = createMockedProvider();
        doBlock(latch, createAnswerExecutingCode(onAnswer)).when(provider).initialize();
        doReturn("blockedProvider").when(provider).toString();
        return provider;
    }

    private Answer<?> createAnswerExecutingCode(Runnable onAnswer) {
        return invocation -> {
            onAnswer.run();
            return null;
        };
    }

    private FeatureProvider unblockingProvider(CountDownLatch latch) {
        FeatureProvider provider = createMockedProvider();
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(provider).initialize();
        doReturn("unblockingProvider").when(provider).toString();
        return provider;
    }
}

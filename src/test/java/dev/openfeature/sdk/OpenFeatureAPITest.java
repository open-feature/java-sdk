package dev.openfeature.sdk;

import java.time.Duration;
import java.util.concurrent.*;

import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.mockito.stubbing.Answer;

import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

class OpenFeatureAPITest {

    private OpenFeatureAPI api;

    @BeforeEach
    void setupTest() {
        api = OpenFeatureAPI.getInstance();
    }

    @Test
    void namedProviderTest() {
        FeatureProvider provider = new NoOpProvider();
        FeatureProviderTestUtils.setFeatureProvider("namedProviderTest", provider);

        assertThat(provider.getMetadata().getName()).isEqualTo(api.getProviderMetadata("namedProviderTest").getName());
    }

    @Test
    void settingDefaultProviderToNullErrors() {
        assertThatCode(() -> api.setProvider(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingNamedClientProviderToNullErrors() {
        assertThatCode(() -> api.setProvider("client-name", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    class InitializationBehavior {

        @Nested
        class DefaultProvider {

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() {
                FeatureProvider featureProvider = mock(FeatureProvider.class);
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
            @DisplayName("should not return set provider if it's initialize method has not yet been finished executing")
            void shouldNotReturnSetProviderIfItsInitializeMethodHasNotYetBeenFinishedExecuting() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider newProvider = mock(FeatureProvider.class);
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
                FeatureProvider fastProvider = unblockProvider(latch);

                api.setProvider(blockedProvider);
                api.setProvider(fastProvider);

                assertThat(testBlockingLatch.await(2, TimeUnit.SECONDS)).as("blocking provider initialization not completed within 2 seconds").isTrue();

                await()
                    .pollDelay(Duration.ofMillis(1))
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(api.getProvider()).isEqualTo(fastProvider));

                verify(blockedProvider, timeout(100)).initialize();
                verify(fastProvider, timeout(100)).initialize();
            }
        }

        @Nested
        class ProviderForNamedClient {

            @Test
            @DisplayName("should immediately return when calling the named client provider mutator")
            void shouldImmediatelyReturnWhenCallingTheNamedClientProviderMutator() {
                FeatureProvider featureProvider = mock(FeatureProvider.class);
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
            @DisplayName("should not return set provider if it's initialize method has not yet been finished executing")
            void shouldNotReturnSetProviderIfItsInitializeMethodHasNotYetBeenFinishedExecuting() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider newProvider = mock(FeatureProvider.class);
                doBlock(latch).when(newProvider).initialize();
                String client = "test client";
                FeatureProvider oldProvider = OpenFeatureAPI.getInstance().getProvider();

                OpenFeatureAPI.getInstance().setProvider(client, newProvider);
                FeatureProvider providerWhileInitialization = OpenFeatureAPI.getInstance().getProvider(client);
                latch.countDown();

                assertThat(providerWhileInitialization).isEqualTo(oldProvider);
                await()
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(OpenFeatureAPI.getInstance().getProvider(client)).isEqualTo(newProvider));
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
                FeatureProvider fastProvider = unblockProvider(latch);

                api.setProvider(clientName, blockedProvider);
                api.setProvider(clientName, fastProvider);

                assertThat(testBlockingLatch.await(2, TimeUnit.SECONDS)).as("blocking provider initialization not completed within 2 seconds").isTrue();

                await()
                    .pollDelay(Duration.ofMillis(1))
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(api.getProvider(clientName)).isEqualTo(fastProvider));

                verify(blockedProvider, timeout(100)).initialize();
                verify(fastProvider, timeout(100)).initialize();
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
                FeatureProvider featureProvider = mock(FeatureProvider.class);
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
            @DisplayName("should set new provider even if shutdown method of replaced one has not yet been finished executing")
            void shouldSetNewProviderEvenIfShutdownMethodOfReplacedOneHasNotYetBeenFinishedExecuting() {
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider blockingProvider = mock(FeatureProvider.class);
                doBlock(latch).when(blockingProvider).shutdown();
                NoOpProvider newProvider = new NoOpProvider();

                FeatureProviderTestUtils.setFeatureProvider(blockingProvider);
                FeatureProviderTestUtils.setFeatureProvider(newProvider);
                latch.countDown();

                await()
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(api.getProvider()).isEqualTo(newProvider));
                verify(blockingProvider, timeout(100)).shutdown();
            }
        }

        @Nested
        class ProviderForNamedClient {

            @Test
            @DisplayName("should immediately return when calling the provider mutator")
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() {
                String clientName = "clientName";
                FeatureProvider featureProvider = mock(FeatureProvider.class);
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).shutdown();
                FeatureProviderTestUtils.setFeatureProvider(clientName, featureProvider);

                await()
                    .alias("wait for provider mutator to return")
                    .atMost(Duration.ofSeconds(1))
                    .until(() -> {
                        api.setProvider(clientName, new NoOpProvider());
                        verify(featureProvider, timeout(100)).shutdown();
                        return true;
                    });

                verify(featureProvider).shutdown();
            }

            @Test
            @DisplayName("should set new provider even if shutdown method of replaced one has not yet been finished executing")
            void shouldSetNewProviderEvenIfShutdownMethodOfReplacedOneHasNotYetBeenFinishedExecuting() {
                String clientName = "clientName";
                CountDownLatch latch = new CountDownLatch(1);
                FeatureProvider blockingProvider = mock(FeatureProvider.class);
                doBlock(latch).when(blockingProvider).shutdown();
                NoOpProvider newProvider = new NoOpProvider();

                FeatureProviderTestUtils.setFeatureProvider(clientName, blockingProvider);
                FeatureProviderTestUtils.setFeatureProvider(clientName, newProvider);
                latch.countDown();

                await()
                    .atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(api.getProvider(clientName)).isEqualTo(newProvider));
                verify(blockingProvider, timeout(100)).shutdown();
            }
        }
    }

    private FeatureProvider blockedProvider(CountDownLatch latch, Runnable onAnswer) {
        FeatureProvider provider = mock(FeatureProvider.class);
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

    private FeatureProvider unblockProvider(CountDownLatch latch) {
        FeatureProvider provider = mock(FeatureProvider.class);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(provider).initialize();
        doReturn("fastProvider").when(provider).toString();
        return provider;
    }
}

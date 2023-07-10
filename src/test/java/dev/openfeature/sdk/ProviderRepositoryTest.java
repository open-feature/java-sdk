package dev.openfeature.sdk;

import static dev.openfeature.sdk.fixtures.ProviderFixture.createMockedProvider;
import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doDelayResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.openfeature.sdk.testutils.exception.TestException;

class ProviderRepositoryTest {

    private static final String CLIENT_NAME = "client name";
    private static final String ANOTHER_CLIENT_NAME = "another client name";
    private static final String FEATURE_KEY = "some key";
    private static final int TIMEOUT = 5000;

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
            @DisplayName("should reject null as default provider")
            void shouldRejectNullAsDefaultProvider() {
                assertThatCode(() -> providerRepository.setProvider(null, mockAfterSet(), mockAfterInit(),
                        mockAfterShutdown(), mockAfterError())).isInstanceOf(IllegalArgumentException.class);
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

                await()
                        .alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(featureProvider, mockAfterSet(), mockAfterInit(),
                                    mockAfterShutdown(), mockAfterError());
                            verify(featureProvider, timeout(TIMEOUT)).initialize(any());
                            return true;
                        });

                verify(featureProvider).initialize(any());
            }

            @Test
            @DisplayName("should avoid additional initialization call if provider has been initialized already")
            void shouldAvoidAdditionalInitializationCallIfProviderHasBeenInitializedAlready() throws Exception {
                FeatureProvider provider = createMockedProvider();
                setFeatureProvider(CLIENT_NAME, provider);

                doReturn(ProviderState.READY).when(provider).getState();

                setFeatureProvider(provider);

                verify(provider).initialize(any());
            }
        }

        @Nested
        class NamedProvider {

            @Test
            @DisplayName("should reject null as named provider")
            void shouldRejectNullAsNamedProvider() {
                assertThatCode(() -> providerRepository.setProvider(CLIENT_NAME, null, mockAfterSet(), mockAfterInit(),
                        mockAfterShutdown(), mockAfterError()))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("should reject null as client name")
            void shouldRejectNullAsDefaultProvider() {
                NoOpProvider provider = new NoOpProvider();
                assertThatCode(() -> providerRepository.setProvider(null, provider, mockAfterSet(), mockAfterInit(),
                        mockAfterShutdown(), mockAfterError()))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("should immediately return when calling the named client provider mutator")
            void shouldImmediatelyReturnWhenCallingTheNamedClientProviderMutator() throws Exception {
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).initialize(any());

                await()
                        .alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider("named client", featureProvider, mockAfterSet(),
                                    mockAfterInit(), mockAfterShutdown(), mockAfterError());
                            verify(featureProvider, timeout(TIMEOUT)).initialize(any());
                            return true;
                        });
            }

            @Test
            @DisplayName("should avoid additional initialization call if provider has been initialized already")
            void shouldAvoidAdditionalInitializationCallIfProviderHasBeenInitializedAlready() throws Exception {
                FeatureProvider provider = createMockedProvider();
                setFeatureProvider(provider);

                doReturn(ProviderState.READY).when(provider).getState();

                setFeatureProvider(CLIENT_NAME, provider);

                verify(provider).initialize(any());
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

                await()
                        .alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(newProvider, mockAfterSet(), mockAfterInit(),
                                    mockAfterShutdown(), mockAfterError());
                            verify(newProvider, timeout(TIMEOUT)).initialize(any());
                            return true;
                        });

                verify(newProvider).initialize(any());
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
            void shouldImmediatelyReturnWhenCallingTheProviderMutator() throws Exception {
                FeatureProvider newProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(newProvider).initialize(any());

                Future<?> providerMutation = executorService
                        .submit(() -> providerRepository.setProvider(CLIENT_NAME, newProvider, mockAfterSet(),
                                mockAfterInit(), mockAfterShutdown(), mockAfterError()));

                await()
                        .alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(providerMutation::isDone);
            }

            @Test
            @DisplayName("should not call shutdown if replaced provider is bound to multiple names")
            void shouldNotCallShutdownIfReplacedProviderIsBoundToMultipleNames() throws InterruptedException {
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

            @Test
            @DisplayName("should not throw exception if provider throws one on shutdown")
            void shouldNotThrowExceptionIfProviderThrowsOneOnShutdown() {
                FeatureProvider provider = createMockedProvider();
                doThrow(TestException.class).when(provider).shutdown();
                setFeatureProvider(provider);

                assertThatCode(() -> setFeatureProvider(new NoOpProvider())).doesNotThrowAnyException();

                verify(provider).shutdown();
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
                .pollDelay(Duration.ofMillis(1))
                .atMost(Duration.ofSeconds(TIMEOUT))
                .untilAsserted(() -> {
                    assertThat(providerRepository.getProvider()).isInstanceOf(NoOpProvider.class);
                    assertThat(providerRepository.getProvider(CLIENT_NAME)).isInstanceOf(NoOpProvider.class);
                    assertThat(providerRepository.getProvider(ANOTHER_CLIENT_NAME)).isInstanceOf(NoOpProvider.class);
                });
        verify(featureProvider1).shutdown();
        verify(featureProvider2).shutdown();
    }

    private void setFeatureProvider(FeatureProvider provider) {
        providerRepository.setProvider(provider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(),
                mockAfterError());
        waitForSettingProviderHasBeenCompleted(ProviderRepository::getProvider, provider);
    }

    private void setFeatureProvider(String namedProvider, FeatureProvider provider) {
        providerRepository.setProvider(namedProvider, provider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(),
                mockAfterError());
        waitForSettingProviderHasBeenCompleted(repository -> repository.getProvider(namedProvider), provider);
    }

    private void waitForSettingProviderHasBeenCompleted(
            Function<ProviderRepository, FeatureProvider> extractor,
            FeatureProvider provider) {
        await()
                .pollDelay(Duration.ofMillis(1))
                .atMost(Duration.ofSeconds(5))
                .until(() -> {
                    return extractor.apply(providerRepository) == provider;
                });
    }

    private Consumer<FeatureProvider> mockAfterSet() {
        return fp -> {
        };
    }

    private Consumer<FeatureProvider> mockAfterInit() {
        return fp -> {
        };
    }

    private Consumer<FeatureProvider> mockAfterShutdown() {
        return fp -> {
        };
    }

    private BiConsumer<FeatureProvider, String> mockAfterError() {
        return (fp, message) -> {
        };
    }

}

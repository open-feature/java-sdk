package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.testutils.exception.TestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.openfeature.sdk.fixtures.ProviderFixture.*;
import static dev.openfeature.sdk.testutils.stubbing.ConditionStubber.doDelayResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProviderRepositoryTest {

    private static final String DOMAIN_NAME = "domain name";
    private static final String ANOTHER_DOMAIN_NAME = "another domain name";
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
                        mockAfterShutdown(), mockAfterError(), false)).isInstanceOf(IllegalArgumentException.class);
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
                                    mockAfterShutdown(), mockAfterError(), false);
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
                assertThatCode(() -> providerRepository.setProvider(DOMAIN_NAME, null, mockAfterSet(), mockAfterInit(),
                        mockAfterShutdown(), mockAfterError(), false))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("should reject null as domain name")
            void shouldRejectNullAsDefaultProvider() {
                NoOpProvider provider = new NoOpProvider();
                assertThatCode(() -> providerRepository.setProvider(null, provider, mockAfterSet(), mockAfterInit(),
                        mockAfterShutdown(), mockAfterError(), false))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("should immediately return when calling the domain provider mutator")
            void shouldImmediatelyReturnWhenCallingTheDomainProviderMutator() throws Exception {
                FeatureProvider featureProvider = createMockedProvider();
                doDelayResponse(Duration.ofSeconds(10)).when(featureProvider).initialize(any());

                await()
                        .alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider("a domain", featureProvider, mockAfterSet(),
                                    mockAfterInit(), mockAfterShutdown(), mockAfterError(), false);
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

                await()
                        .alias("wait for provider mutator to return")
                        .pollDelay(Duration.ofMillis(1))
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> {
                            providerRepository.setProvider(newProvider, mockAfterSet(), mockAfterInit(),
                                    mockAfterShutdown(), mockAfterError(), false);
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

                Future<?> providerMutation = executorService
                        .submit(() -> providerRepository.setProvider(DOMAIN_NAME, newProvider, mockAfterSet(),
                                mockAfterInit(), mockAfterShutdown(), mockAfterError(), false));

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
        providerRepository.setProvider(provider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(),
                mockAfterError(), false);
        waitForSettingProviderHasBeenCompleted(ProviderRepository::getProvider, provider);
    }


    private void setFeatureProvider(FeatureProvider provider, Consumer<FeatureProvider> afterSet,
                                    Consumer<FeatureProvider> afterInit, Consumer<FeatureProvider> afterShutdown,
                                    BiConsumer<FeatureProvider, OpenFeatureError> afterError) {
        providerRepository.setProvider(provider, afterSet, afterInit, afterShutdown,
                afterError, false);
        waitForSettingProviderHasBeenCompleted(ProviderRepository::getProvider, provider);
    }

    private void setFeatureProvider(String namedProvider, FeatureProvider provider) {
        providerRepository.setProvider(namedProvider, provider, mockAfterSet(), mockAfterInit(), mockAfterShutdown(),
                mockAfterError(), false);
        waitForSettingProviderHasBeenCompleted(repository -> repository.getProvider(namedProvider), provider);
    }

    private void waitForSettingProviderHasBeenCompleted(
            Function<ProviderRepository, FeatureProvider> extractor,
            FeatureProvider provider) {
        await()
                .pollDelay(Duration.ofMillis(1))
                .atMost(Duration.ofSeconds(5))
                .until(() -> {
                    return extractor.apply(providerRepository).equals(provider);
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

    private BiConsumer<FeatureProvider, OpenFeatureError> mockAfterError() {
        return (fp, ex) -> {
        };
    }

}

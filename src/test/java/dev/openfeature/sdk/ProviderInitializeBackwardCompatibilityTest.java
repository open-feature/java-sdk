package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProviderInitializeBackwardCompatibilityTest {

    private static final String DOMAIN = "billing";

    @Nested
    @DisplayName("FeatureProvider default method delegation")
    class DefaultMethodDelegation {

        @Test
        @DisplayName("two-arg default delegates to a single-arg override")
        void twoArgDefaultDelegatesToSingleArgOverride() throws Exception {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            provider.initialize(new ImmutableContext(), DOMAIN);

            assertThat(singleArgInitCount).hasValue(1);
        }

        @Test
        @DisplayName("two-arg override is used without invoking single-arg")
        void twoArgOverrideDoesNotInvokeSingleArg() throws Exception {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            AtomicReference<String> domainReceived = new AtomicReference<>();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext, String domain) {
                    domainReceived.set(domain);
                }

                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            provider.initialize(new ImmutableContext(), DOMAIN);

            assertThat(domainReceived).hasValue(DOMAIN);
            assertThat(singleArgInitCount).hasValue(0);
        }

        @Test
        @DisplayName("two-arg-only override receives null domain for default binding")
        void twoArgOnlyOverrideReceivesNullDomain() throws Exception {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            AtomicReference<String> domainReceived = new AtomicReference<>("unset");
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext, String domain) {
                    domainReceived.set(domain);
                }

                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            provider.initialize(new ImmutableContext(), null);

            assertThat(domainReceived.get()).isNull();
            assertThat(singleArgInitCount).hasValue(0);
        }

        @Test
        @DisplayName("single-arg override is only invoked once per initialization")
        void singleArgOverrideIsOnlyInvokedOnce() throws Exception {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            provider.initialize(new ImmutableContext(), DOMAIN);
            provider.initialize(new ImmutableContext(), DOMAIN);

            assertThat(singleArgInitCount).hasValue(2);
        }
    }

    @Nested
    @DisplayName("SDK initialization path")
    class SdkIntegration {

        private OpenFeatureAPI api;

        @BeforeEach
        void setupTest() {
            api = new OpenFeatureAPI();
            api.setProvider(new NoOpProvider());
        }

        @Test
        @DisplayName("legacy single-arg provider is initialized when registered to a named domain")
        void legacySingleArgProviderInitializedForNamedDomain() {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            api.setProvider(DOMAIN, provider);

            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(singleArgInitCount).hasValue(1));
        }

        @Test
        @DisplayName("legacy single-arg provider is initialized when registered as the default provider")
        void legacySingleArgProviderInitializedAsDefault() {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            api.setProvider(provider);

            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(singleArgInitCount).hasValue(1));
        }

        @Test
        @DisplayName("two-arg-only provider receives the bound domain from the SDK")
        void twoArgOnlyProviderReceivesBoundDomainFromSdk() {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            AtomicReference<String> domainReceived = new AtomicReference<>();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext, String domain) {
                    domainReceived.set(domain);
                }

                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            api.setProvider(DOMAIN, provider);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertThat(domainReceived).hasValue(DOMAIN);
                assertThat(singleArgInitCount).hasValue(0);
            });
        }

        @Test
        @DisplayName("two-arg-only default provider receives null domain from the SDK")
        void twoArgOnlyDefaultProviderReceivesNullDomainFromSdk() {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            AtomicReference<String> domainReceived = new AtomicReference<>("unset");
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext, String domain) {
                    domainReceived.set(domain);
                }

                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };

            api.setProvider(provider);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertThat(domainReceived.get()).isNull();
                assertThat(singleArgInitCount).hasValue(0);
            });
        }
    }

    @Nested
    @DisplayName("FeatureProviderStateManager")
    class StateManager {

        @Test
        @DisplayName("delegates two-arg initialize to a legacy single-arg provider")
        void delegatesTwoArgInitializeToLegacySingleArgProvider() throws Exception {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };
            FeatureProviderStateManager stateManager = new FeatureProviderStateManager(provider);

            stateManager.initialize(new ImmutableContext(), DOMAIN);

            assertThat(singleArgInitCount).hasValue(1);
        }

        @Test
        @DisplayName("only invokes two-arg initialize once for a legacy single-arg provider")
        void onlyInvokesLegacySingleArgProviderOncePerStateManagerInit() throws Exception {
            AtomicInteger singleArgInitCount = new AtomicInteger();
            FeatureProvider provider = new TestProvider() {
                @Override
                public void initialize(EvaluationContext evaluationContext) {
                    singleArgInitCount.incrementAndGet();
                }
            };
            FeatureProviderStateManager stateManager = new FeatureProviderStateManager(provider);

            stateManager.initialize(new ImmutableContext(), DOMAIN);
            stateManager.initialize(new ImmutableContext(), DOMAIN);

            assertThat(singleArgInitCount).hasValue(1);
        }
    }

    private abstract static class TestProvider extends EventProvider {

        @Override
        public Metadata getMetadata() {
            return () -> "initialize-backward-compat-test";
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Boolean>builder().value(defaultValue).build();
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<String>builder().value(defaultValue).build();
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Integer>builder().value(defaultValue).build();
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Double>builder().value(defaultValue).build();
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Value>builder().value(defaultValue).build();
        }
    }
}

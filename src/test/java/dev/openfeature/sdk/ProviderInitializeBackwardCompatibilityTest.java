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
        @DisplayName("default isDomainScoped returns false for non-domain-scoped providers")
        void defaultIsDomainScopedReturnsFalse() {
            assertThat(new LegacySingleArgInitProvider().isDomainScoped()).isFalse();
        }

        @Test
        @DisplayName("two-arg default delegates to a single-arg override")
        void twoArgDefaultDelegatesToSingleArgOverride() throws Exception {
            LegacySingleArgInitProvider provider = new LegacySingleArgInitProvider();

            provider.initialize(new ImmutableContext(), DOMAIN);

            assertThat(provider.singleArgInitCount()).isOne();
        }

        @Test
        @DisplayName("two-arg override is used without invoking single-arg")
        void twoArgOverrideDoesNotInvokeSingleArg() throws Exception {
            TwoArgInitProvider provider = new TwoArgInitProvider();

            provider.initialize(new ImmutableContext(), DOMAIN);

            assertThat(provider.initDomain()).isEqualTo(DOMAIN);
            assertThat(provider.singleArgInitCount()).isZero();
        }

        @Test
        @DisplayName("two-arg-only override receives null domain for default binding")
        void twoArgOnlyOverrideReceivesNullDomain() throws Exception {
            TwoArgInitProvider provider = new TwoArgInitProvider();

            provider.initialize(new ImmutableContext(), null);

            assertThat(provider.initDomain()).isNull();
            assertThat(provider.singleArgInitCount()).isZero();
        }

        @Test
        @DisplayName("single-arg override is only invoked once per initialization")
        void singleArgOverrideIsOnlyInvokedOnce() throws Exception {
            LegacySingleArgInitProvider provider = new LegacySingleArgInitProvider();

            provider.initialize(new ImmutableContext(), DOMAIN);
            provider.initialize(new ImmutableContext(), DOMAIN);

            assertThat(provider.singleArgInitCount()).isEqualTo(2);
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
            LegacySingleArgInitProvider provider = new LegacySingleArgInitProvider();

            api.setProvider(DOMAIN, provider);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(provider.singleArgInitCount())
                    .isOne());
        }

        @Test
        @DisplayName("legacy single-arg provider is initialized when registered as the default provider")
        void legacySingleArgProviderInitializedAsDefault() {
            LegacySingleArgInitProvider provider = new LegacySingleArgInitProvider();

            api.setProvider(provider);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(provider.singleArgInitCount())
                    .isOne());
        }

        @Test
        @DisplayName("two-arg-only provider receives the bound domain from the SDK")
        void twoArgOnlyProviderReceivesBoundDomainFromSdk() {
            TwoArgInitProvider provider = new TwoArgInitProvider();

            api.setProvider(DOMAIN, provider);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertThat(provider.initDomain()).isEqualTo(DOMAIN);
                assertThat(provider.singleArgInitCount()).isZero();
            });
        }

        @Test
        @DisplayName("two-arg-only default provider receives null domain from the SDK")
        void twoArgOnlyDefaultProviderReceivesNullDomainFromSdk() {
            TwoArgInitProvider provider = new TwoArgInitProvider();

            api.setProvider(provider);

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertThat(provider.initDomain()).isNull();
                assertThat(provider.singleArgInitCount()).isZero();
            });
        }
    }

    @Nested
    @DisplayName("FeatureProviderStateManager")
    class StateManager {

        @Test
        @DisplayName("delegates two-arg initialize to a legacy single-arg provider")
        void delegatesTwoArgInitializeToLegacySingleArgProvider() throws Exception {
            LegacySingleArgInitProvider provider = new LegacySingleArgInitProvider();
            FeatureProviderStateManager stateManager = new FeatureProviderStateManager(provider);

            stateManager.initialize(new ImmutableContext(), DOMAIN);

            assertThat(provider.singleArgInitCount()).isOne();
        }

        @Test
        @DisplayName("only invokes two-arg initialize once for a legacy single-arg provider")
        void onlyInvokesLegacySingleArgProviderOncePerStateManagerInit() throws Exception {
            LegacySingleArgInitProvider provider = new LegacySingleArgInitProvider();
            FeatureProviderStateManager stateManager = new FeatureProviderStateManager(provider);

            stateManager.initialize(new ImmutableContext(), DOMAIN);
            stateManager.initialize(new ImmutableContext(), DOMAIN);

            assertThat(provider.singleArgInitCount()).isOne();
        }
    }

    /**
     * Legacy provider that only overrides single-arg {@link FeatureProvider#initialize(EvaluationContext)}.
     */
    private static final class LegacySingleArgInitProvider extends StubProvider {

        private final AtomicInteger singleArgInitCount = new AtomicInteger();

        @Override
        public void initialize(EvaluationContext evaluationContext) {
            singleArgInitCount.incrementAndGet();
        }

        int singleArgInitCount() {
            return singleArgInitCount.get();
        }
    }

    /**
     * Domain-aware provider that only overrides two-arg
     * {@link FeatureProvider#initialize(EvaluationContext, String)}. Single-arg is overridden solely to detect
     * accidental delegation.
     */
    private static final class TwoArgInitProvider extends StubProvider {

        private final AtomicInteger singleArgInitCount = new AtomicInteger();
        private final AtomicReference<String> initDomain = new AtomicReference<>();

        @Override
        public void initialize(EvaluationContext evaluationContext, String domain) {
            initDomain.set(domain);
        }

        @Override
        public void initialize(EvaluationContext evaluationContext) {
            singleArgInitCount.incrementAndGet();
        }

        int singleArgInitCount() {
            return singleArgInitCount.get();
        }

        String initDomain() {
            return initDomain.get();
        }
    }

    private abstract static class StubProvider extends EventProvider {

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

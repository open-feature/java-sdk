package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainScopedProviderSpecTest {

    private static final String DOMAIN_A = "domain-a";
    private static final String DOMAIN_B = "domain-b";
    private OpenFeatureAPI api;

    @BeforeEach
    void setupTest() {
        api = new OpenFeatureAPI();
        api.setProvider(new NoOpProvider());
    }

    @Specification(
            number = "1.1.8.1",
            text = "The `provider mutator` MUST NOT bind a `domain-scoped` provider instance to more than one "
                    + "`domain`, rejecting any attempt to bind an already-bound instance to an additional `domain`.")
    @Test
    @DisplayName("rejects binding a domain-scoped provider to a second named domain")
    void rejectsBindingDomainScopedProviderToSecondNamedDomain() {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProvider(DOMAIN_A, provider);

        assertThatThrownBy(() -> api.setProvider(DOMAIN_B, provider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Domain-scoped provider cannot be bound to more than one domain");
    }

    @Specification(
            number = "1.1.8.1",
            text = "The `provider mutator` MUST NOT bind a `domain-scoped` provider instance to more than one "
                    + "`domain`, rejecting any attempt to bind an already-bound instance to an additional `domain`.")
    @Test
    @DisplayName("rejects binding a domain-scoped named provider as the default provider")
    void rejectsBindingDomainScopedNamedProviderAsDefault() {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProvider(DOMAIN_A, provider);

        assertThatThrownBy(() -> api.setProvider(provider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Domain-scoped provider cannot be bound to more than one domain");
    }

    @Specification(
            number = "1.1.8.1",
            text = "The `provider mutator` MUST NOT bind a `domain-scoped` provider instance to more than one "
                    + "`domain`, rejecting any attempt to bind an already-bound instance to an additional `domain`.")
    @Test
    @DisplayName("rejects binding a domain-scoped default provider to a named domain")
    void rejectsBindingDomainScopedDefaultProviderToNamedDomain() {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProvider(provider);

        assertThatThrownBy(() -> api.setProvider(DOMAIN_A, provider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Domain-scoped provider cannot be bound to more than one domain");
    }

    @Test
    @DisplayName("allows rebinding a domain-scoped provider to the same named domain")
    void allowsRebindingDomainScopedProviderToSameNamedDomain() throws Exception {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();
        FeatureProvider replacement = mock(FeatureProvider.class);
        doReturn(ProviderState.NOT_READY).when(replacement).getState();
        doReturn(true).when(replacement).isDomainScoped();

        api.setProvider(DOMAIN_A, provider);

        assertThatCode(() -> api.setProvider(DOMAIN_A, replacement)).doesNotThrowAnyException();
        verify(replacement, timeout(1000)).initialize(any(), eq(DOMAIN_A));
    }

    @Test
    @DisplayName("allows binding a non-domain-scoped provider to multiple domains")
    void allowsBindingNonDomainScopedProviderToMultipleDomains() throws Exception {
        FeatureProvider provider = mock(FeatureProvider.class);
        doReturn(ProviderState.NOT_READY).when(provider).getState();

        assertThatCode(() -> {
                    api.setProvider(DOMAIN_A, provider);
                    api.setProvider(DOMAIN_B, provider);
                })
                .doesNotThrowAnyException();

        verify(provider, timeout(1000)).initialize(any(), eq(DOMAIN_A));
    }

    @Specification(
            number = "2.4.3",
            text =
                    "The `provider` MAY declare that it is `domain-scoped`, indicating that it maintains state "
                            + "specific to a single `domain`, such as a persistent cache, that cannot be shared across `domains`.")
    @Test
    @DisplayName("domain-scoped provider receives bound domain at initialization")
    void domainScopedProviderReceivesBoundDomainAtInitialization() {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProvider(DOMAIN_A, provider);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(provider.initDomain.get()).isEqualTo(DOMAIN_A));
    }

    private static final class DomainScopedTestProvider extends EventProvider {

        private final AtomicReference<String> initDomain = new AtomicReference<>();

        @Override
        public Metadata getMetadata() {
            return () -> "domain-scoped-test";
        }

        @Override
        public boolean isDomainScoped() {
            return true;
        }

        @Override
        public void initialize(EvaluationContext evaluationContext, String domain) {
            initDomain.set(domain);
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

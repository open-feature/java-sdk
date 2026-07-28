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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        api.shutdown();
    }

    @Specification(
            number = "1.1.8.1",
            text = "The `provider mutator` MUST NOT bind a `domain-scoped` provider instance to more than one "
                    + "`domain`, rejecting any attempt to bind an already-bound instance to an additional `domain`.")
    @Test
    @DisplayName("rejects binding a domain-scoped provider to a second named domain")
    void rejectsBindingDomainScopedProviderToSecondNamedDomain() throws Exception {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProviderAndWait(DOMAIN_A, provider);

        assertThatThrownBy(() -> api.setProvider(DOMAIN_B, provider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Domain-scoped provider cannot be bound to more than one domain");
        assertThat(provider.initCount()).isOne();
    }

    @Specification(
            number = "1.1.8.1",
            text = "The `provider mutator` MUST NOT bind a `domain-scoped` provider instance to more than one "
                    + "`domain`, rejecting any attempt to bind an already-bound instance to an additional `domain`.")
    @Test
    @DisplayName("rejects binding a domain-scoped named provider as the default provider")
    void rejectsBindingDomainScopedNamedProviderAsDefault() throws Exception {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProviderAndWait(DOMAIN_A, provider);

        assertThatThrownBy(() -> api.setProvider(provider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Domain-scoped provider cannot be bound to more than one domain");
        assertThat(provider.initCount()).isOne();
    }

    @Specification(
            number = "1.1.8.1",
            text = "The `provider mutator` MUST NOT bind a `domain-scoped` provider instance to more than one "
                    + "`domain`, rejecting any attempt to bind an already-bound instance to an additional `domain`.")
    @Test
    @DisplayName("rejects binding a domain-scoped default provider to a named domain")
    void rejectsBindingDomainScopedDefaultProviderToNamedDomain() throws Exception {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProviderAndWait(provider);

        assertThatThrownBy(() -> api.setProvider(DOMAIN_A, provider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Domain-scoped provider cannot be bound to more than one domain");
        assertThat(provider.initCount()).isOne();
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
    @DisplayName("allows rebinding the same domain-scoped provider instance to the same named domain")
    void allowsRebindingSameDomainScopedInstanceToSameNamedDomain() throws Exception {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProviderAndWait(DOMAIN_A, provider);
        api.setProviderAndWait(DOMAIN_A, provider);

        assertThat(provider.initCount()).isOne();
        assertThat(provider.initDomain.get()).isEqualTo(DOMAIN_A);
    }

    @Test
    @DisplayName("allows rebinding the same domain-scoped provider instance as the default provider")
    void allowsRebindingSameDomainScopedInstanceAsDefault() throws Exception {
        DomainScopedTestProvider provider = new DomainScopedTestProvider();

        api.setProviderAndWait(provider);
        api.setProviderAndWait(provider);

        assertThat(provider.initCount()).isOne();
        assertThat(provider.initDomain.get()).isNull();
    }

    @Test
    @DisplayName("allows equal-but-distinct domain-scoped instances on separate domains")
    void allowsEqualDistinctDomainScopedInstancesOnSeparateDomains() throws Exception {
        EqualDomainScopedProvider providerA = new EqualDomainScopedProvider("shared-key");
        EqualDomainScopedProvider providerB = new EqualDomainScopedProvider("shared-key");

        assertThat(providerA).isEqualTo(providerB);
        assertThat(providerA).isNotSameAs(providerB);

        api.setProviderAndWait(DOMAIN_A, providerA);

        assertThatCode(() -> api.setProviderAndWait(DOMAIN_B, providerB)).doesNotThrowAnyException();
        assertThat(api.getProvider(DOMAIN_A)).isSameAs(providerA);
        assertThat(api.getProvider(DOMAIN_B)).isSameAs(providerB);
    }

    @Test
    @DisplayName("allows binding a non-domain-scoped provider to multiple domains")
    void allowsBindingNonDomainScopedProviderToMultipleDomains() throws Exception {
        FeatureProvider provider = mock(FeatureProvider.class);
        doReturn(ProviderState.NOT_READY).when(provider).getState();
        Metadata metadata = mock(Metadata.class);
        doReturn("shared-provider").when(metadata).getName();
        doReturn(metadata).when(provider).getMetadata();

        api.setProviderAndWait(DOMAIN_A, provider);
        api.setProviderAndWait(DOMAIN_B, provider);

        assertThat(api.getProvider(DOMAIN_A)).isSameAs(provider);
        assertThat(api.getProvider(DOMAIN_B)).isSameAs(provider);
        verify(provider, times(1)).initialize(any(), eq(DOMAIN_A));
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

    private static class DomainScopedTestProvider extends EventProvider {

        private final AtomicReference<String> initDomain = new AtomicReference<>();
        private final AtomicInteger initializeCount = new AtomicInteger();

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
            initializeCount.incrementAndGet();
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

        int initCount() {
            return initializeCount.get();
        }
    }

    private static final class EqualDomainScopedProvider extends DomainScopedTestProvider {

        private final String key;

        EqualDomainScopedProvider(String key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EqualDomainScopedProvider)) {
                return false;
            }
            EqualDomainScopedProvider other = (EqualDomainScopedProvider) obj;
            return key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}

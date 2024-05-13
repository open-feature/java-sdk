package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;

class OpenFeatureAPITest {

    private static final String DOMAIN_NAME = "my domain";

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

    @Specification(number="1.1.3", text="The API MUST provide a function to bind a given provider to one or more clients using a domain. If the domain already has a bound provider, it is overwritten with the new mapping.")
    @Test
    void namedProviderOverwrittenTest() {
        String domain = "namedProviderOverwrittenTest";
        FeatureProvider provider1 = new NoOpProvider();
        FeatureProvider provider2 = new DoSomethingProvider();
        FeatureProviderTestUtils.setFeatureProvider(domain, provider1);
        FeatureProviderTestUtils.setFeatureProvider(domain, provider2);

        assertThat(OpenFeatureAPI.getInstance().getProvider(domain).getMetadata().getName())
                .isEqualTo(DoSomethingProvider.name);
    }

    @Test
    void providerToMultipleNames() throws Exception {
        FeatureProvider inMemAsEventingProvider = new InMemoryProvider(Collections.EMPTY_MAP);
        FeatureProvider noOpAsNonEventingProvider = new NoOpProvider();

        // register same provider for multiple names & as default provider
        OpenFeatureAPI.getInstance().setProviderAndWait(inMemAsEventingProvider);
        OpenFeatureAPI.getInstance().setProviderAndWait("clientA", inMemAsEventingProvider);
        OpenFeatureAPI.getInstance().setProviderAndWait("clientB", inMemAsEventingProvider);
        OpenFeatureAPI.getInstance().setProviderAndWait("clientC", noOpAsNonEventingProvider);
        OpenFeatureAPI.getInstance().setProviderAndWait("clientD", noOpAsNonEventingProvider);

        assertEquals(inMemAsEventingProvider, OpenFeatureAPI.getInstance().getProvider());
        assertEquals(inMemAsEventingProvider, OpenFeatureAPI.getInstance().getProvider("clientA"));
        assertEquals(inMemAsEventingProvider, OpenFeatureAPI.getInstance().getProvider("clientB"));
        assertEquals(noOpAsNonEventingProvider, OpenFeatureAPI.getInstance().getProvider("clientC"));
        assertEquals(noOpAsNonEventingProvider, OpenFeatureAPI.getInstance().getProvider("clientD"));
    }

    @Test
    void settingDefaultProviderToNullErrors() {
        assertThatCode(() -> api.setProvider(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingDomainProviderToNullErrors() {
        assertThatCode(() -> api.setProvider(DOMAIN_NAME, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingTransactionalContextPropagatorToNullErrors() {
        assertThatCode(() -> api.setTransactionContextPropagator(null)).isInstanceOf(IllegalArgumentException.class);
    }
}

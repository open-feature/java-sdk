package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import dev.openfeature.sdk.testutils.TestEventsProvider;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenFeatureAPITest {

    private static final String DOMAIN_NAME = "my domain";

    private OpenFeatureAPI api;

    @BeforeEach
    void setupTest() {
        api = new OpenFeatureAPI();
    }

    @Test
    void namedProviderTest() {
        FeatureProvider provider = new NoOpProvider();
        api.setProviderAndWait("namedProviderTest", provider);

        assertThat(provider.getMetadata().getName())
                .isEqualTo(api.getProviderMetadata("namedProviderTest").getName());
    }

    @Specification(
            number = "1.1.3",
            text =
                    "The API MUST provide a function to bind a given provider to one or more clients using a domain. If the domain already has a bound provider, it is overwritten with the new mapping.")
    @Test
    void namedProviderOverwrittenTest() {
        String domain = "namedProviderOverwrittenTest";
        FeatureProvider provider1 = new NoOpProvider();
        FeatureProvider provider2 = new DoSomethingProvider();
        api.setProviderAndWait(domain, provider1);
        api.setProviderAndWait(domain, provider2);

        assertThat(api.getProvider(domain).getMetadata().getName()).isEqualTo(DoSomethingProvider.name);
    }

    @Test
    void providerToMultipleNames() throws Exception {
        FeatureProvider inMemAsEventingProvider = new InMemoryProvider(Collections.EMPTY_MAP);
        FeatureProvider noOpAsNonEventingProvider = new NoOpProvider();

        // register same provider for multiple names & as default provider
        api.setProviderAndWait(inMemAsEventingProvider);
        api.setProviderAndWait("clientA", inMemAsEventingProvider);
        api.setProviderAndWait("clientB", inMemAsEventingProvider);
        api.setProviderAndWait("clientC", noOpAsNonEventingProvider);
        api.setProviderAndWait("clientD", noOpAsNonEventingProvider);

        assertEquals(inMemAsEventingProvider, api.getProvider());
        assertEquals(inMemAsEventingProvider, api.getProvider("clientA"));
        assertEquals(inMemAsEventingProvider, api.getProvider("clientB"));
        assertEquals(noOpAsNonEventingProvider, api.getProvider("clientC"));
        assertEquals(noOpAsNonEventingProvider, api.getProvider("clientD"));
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

    @Test
    void setEvaluationContextShouldAllowChaining() {
        OpenFeatureClient client = new OpenFeatureClient(api, "name", "version");
        EvaluationContext ctx = new ImmutableContext("targeting key", new HashMap<>());
        OpenFeatureClient result = client.setEvaluationContext(ctx);
        assertEquals(client, result);
    }

    @Test
    void getStateReturnsTheStateOfTheAppropriateProvider() throws Exception {
        String domain = "namedProviderOverwrittenTest";
        FeatureProvider provider1 = new NoOpProvider();
        FeatureProvider provider2 = new TestEventsProvider();
        api.setProviderAndWait(domain, provider1);
        api.setProviderAndWait(domain, provider2);

        provider2.initialize(null);

        assertThat(api.getClient(domain).getProviderState()).isEqualTo(ProviderState.READY);
    }

    @Test
    void featureProviderTrackIsCalled() throws Exception {
        FeatureProvider featureProvider = mock(FeatureProvider.class);
        api.setProviderAndWait(featureProvider);

        api.getClient().track("track-event", new ImmutableContext(), new MutableTrackingEventDetails(22.2f));

        verify(featureProvider).initialize(any());
        verify(featureProvider, atLeastOnce()).getMetadata();
        verify(featureProvider).track(any(), any(), any());
    }
}

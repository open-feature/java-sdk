package dev.openfeature.sdk;

import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientProviderMappingTest {

    @Test
    void clientProviderTest() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();

        FeatureProviderTestUtils.setFeatureProvider("client1", new DoSomethingProvider());
        FeatureProviderTestUtils.setFeatureProvider("client2", new NoOpProvider());

        Client c1 = api.getClient("client1");
        Client c2 = api.getClient("client2");

        assertTrue(c1.getBooleanValue("test", false));
        assertFalse(c2.getBooleanValue("test", false));
    }
}

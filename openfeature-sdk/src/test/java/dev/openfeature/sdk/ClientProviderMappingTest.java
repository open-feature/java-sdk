package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClientProviderMappingTest {

    @Test
    void clientProviderTest() {
        OpenFeatureAPI api = new OpenFeatureAPI();

        api.setProviderAndWait("client1", new DoSomethingProvider());
        api.setProviderAndWait("client2", new NoOpProvider());

        Client c1 = api.getClient("client1");
        Client c2 = api.getClient("client2");

        assertTrue(c1.getBooleanValue("test", false));
        assertFalse(c2.getBooleanValue("test", false));
    }
}

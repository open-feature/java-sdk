package dev.openfeature.sdk;

import io.cucumber.java.eo.Do;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientProviderMappingTest {
    @Test
    void clientProviderTest() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();

        api.setProvider("client1", new DoSomethingProvider());
        api.setProvider("client2", new NoOpProvider());

        Client c1 = api.getClient("client1");
        Client c2 = api.getClient("client2");

        assertTrue(c1.getBooleanValue("test", false));
        assertFalse(c2.getBooleanValue("test", false));
    }
}

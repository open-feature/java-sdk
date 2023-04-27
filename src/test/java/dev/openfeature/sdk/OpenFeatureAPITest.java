package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenFeatureAPITest {
    @Test
    void namedProviderTest() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProvider provider = new NoOpProvider();
        api.setProvider("namedProviderTest", provider);
        assertEquals(provider.getMetadata().getName(), api.getProviderMetadata("namedProviderTest").getName());
    }
}

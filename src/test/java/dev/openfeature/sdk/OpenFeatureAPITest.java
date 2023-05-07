package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OpenFeatureAPITest {
    @Test
    void namedProviderTest() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProvider provider = new NoOpProvider();
        api.setProvider("namedProviderTest", provider);
        assertEquals(provider.getMetadata().getName(), api.getProviderMetadata("namedProviderTest").getName());
    }

    @Test void settingDefaultProviderToNullErrors() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        assertThrows(IllegalArgumentException.class, () -> api.setProvider(null));
    }

    @Test void settingNamedClientProviderToNullErrors() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        assertThrows(IllegalArgumentException.class, () -> api.setProvider("client-name", null));
    }
}

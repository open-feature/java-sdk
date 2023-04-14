package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenFeatureAPITest {
    @Test
    void namedProviderTest() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        AlwaysBrokenProvider provider = new AlwaysBrokenProvider();
        api.setProvider("namedProviderTest", provider);
        assertEquals(provider.getMetadata(), api.getProviderMetadata("namedProviderTest"));
    }
}

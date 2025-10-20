package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;

import dev.openfeature.sdk.testutils.testProvider.TestProvider;
import org.junit.jupiter.api.Test;

class ClientProviderMappingTest {

    @Test
    void clientProviderTest() {
        OpenFeatureAPI api = new OpenFeatureAPI();

        var provider1 = TestProvider.builder().initsToReady();
        var provider2 = TestProvider.builder().initsToReady();

        api.setProviderAndWait("client1", provider1);
        api.setProviderAndWait("client2", provider2);

        Client c1 = api.getClient("client1");
        Client c2 = api.getClient("client2");

        c1.getBooleanValue("test", false);
        c2.getBooleanValue("test", false);

        assertEquals(1, provider1.getFlagEvaluations().size());
        assertEquals(1, provider2.getFlagEvaluations().size());
    }
}

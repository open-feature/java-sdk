package dev.openfeature.sdk.providers.memory;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.Value;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.openfeature.sdk.Structure.mapToStructure;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.buildFlags;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryProviderTest {

    private static Client client;

    private static final AtomicBoolean isProviderReady = new AtomicBoolean(false);

    private static final AtomicInteger configurationChangedEventCount = new AtomicInteger(0);

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        Map<String, Flag<?>> flags = buildFlags();
        InMemoryProvider provider = new InMemoryProvider(flags);
        OpenFeatureAPI.getInstance().setProvider(provider);

        // TODO: setProvider with wait for init, pending https://github.com/open-feature/ofep/pull/80
        Thread.sleep(500);

        client = OpenFeatureAPI.getInstance().getClient();

        client.onProviderReady(eventDetails -> isProviderReady.set(true));

        client.onProviderConfigurationChanged(eventDetails -> configurationChangedEventCount.incrementAndGet());
        provider.updateFlags(flags);
        provider.updateFlag("addedFlag", Flag.builder()
            .variant("on", true)
            .variant("off", false)
            .defaultVariant("on")
            .build());
    }

    @SneakyThrows
    @Test
    void eventsTest() {
        assertTrue(isProviderReady.get());
        assertEquals(2, configurationChangedEventCount.get());
    }

    @Test
    void getBooleanEvaluation() {
        assertTrue(client.getBooleanValue("boolean-flag", false));
    }

    @Test
    void getStringEvaluation() {
        assertEquals("hi", client.getStringValue("string-flag", "dummy"));
    }

    @Test
    void getIntegerEvaluation() {
        assertEquals(10, client.getIntegerValue("integer-flag", 999));
    }

    @Test
    void getDoubleEvaluation() {
        assertEquals(0.5, client.getDoubleValue("float-flag", 9.99));
    }

    @Test
    void getObjectEvaluation() {
        Value expectedObject = new Value(mapToStructure(ImmutableMap.of(
            "showImages", new Value(true),
            "title", new Value("Check out these pics!"),
            "imagesPerPage", new Value(100)
        )));
        assertEquals(expectedObject, client.getObjectValue("object-flag", new Value(true)));
    }

}
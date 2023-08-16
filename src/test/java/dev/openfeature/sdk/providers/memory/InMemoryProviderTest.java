package dev.openfeature.sdk.providers.memory;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.exceptions.TypeMismatchError;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.omg.CORBA.DynAnyPackage.TypeMismatch;

import java.util.Map;

import static dev.openfeature.sdk.Structure.mapToStructure;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.buildFlags;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class InMemoryProviderTest {

    private static Client client;

    private static InMemoryProvider provider;

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        Map<String, Flag<?>> flags = buildFlags();
        provider = spy(new InMemoryProvider(flags));
        OpenFeatureAPI.getInstance().onProviderConfigurationChanged(eventDetails -> {});
        OpenFeatureAPI.getInstance().setProvider(provider);

        // TODO: setProvider with wait for init, pending https://github.com/open-feature/ofep/pull/80
        Thread.sleep(500);

        client = OpenFeatureAPI.getInstance().getClient();
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
        verify(provider, times(2)).emitProviderConfigurationChanged(any());
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

    @Test
    void notFound() {
        assertThrows(FlagNotFoundError.class, () -> {
            provider.getBooleanEvaluation("not-found-flag", false, new ImmutableContext());
        });
    }

    @Test
    void typeMismatch() {
        assertThrows(TypeMismatchError.class, () -> {
            provider.getBooleanEvaluation("string-flag", false, new ImmutableContext());
        });
    }
}
package dev.openfeature.sdk.providers.memory;

import static dev.openfeature.sdk.Structure.mapToStructure;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.buildFlags;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EventDetails;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.OpenFeatureAPITestUtil;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.exceptions.ProviderNotReadyError;
import dev.openfeature.sdk.exceptions.TypeMismatchError;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryProviderTest {

    private Client client;

    private InMemoryProvider provider;
    private OpenFeatureAPI api;

    @SneakyThrows
    @BeforeEach
    void beforeEach() {
        Map<String, Flag<?>> flags = buildFlags();
        provider = spy(new InMemoryProvider(flags));
        api = OpenFeatureAPITestUtil.createAPI();
        api.onProviderConfigurationChanged(eventDetails -> {});
        api.setProviderAndWait(provider);
        client = api.getClient();
        provider.updateFlags(flags);
        provider.updateFlag(
                "addedFlag",
                Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("on")
                        .build());
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
                "imagesPerPage", new Value(100))));
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

    @SneakyThrows
    @Test
    void shouldThrowIfNotInitialized() {
        InMemoryProvider inMemoryProvider = new InMemoryProvider(new HashMap<>());

        // ErrorCode.PROVIDER_NOT_READY should be returned when evaluated via the client
        assertThrows(
                ProviderNotReadyError.class,
                () -> inMemoryProvider.getBooleanEvaluation("fail_not_initialized", false, new ImmutableContext()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void emitChangedFlagsOnlyIfThereAreChangedFlags() {
        Consumer<EventDetails> handler = mock(Consumer.class);
        Map<String, Flag<?>> flags = buildFlags();

        api.onProviderConfigurationChanged(handler);
        api.setProviderAndWait(provider);

        provider.updateFlags(flags);

        await().untilAsserted(() -> verify(handler, times(1))
                .accept(argThat(details ->
                        details.getFlagsChanged().size() == buildFlags().size())));
    }
}

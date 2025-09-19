package dev.openfeature.sdk.providers.memory;

import static dev.openfeature.api.types.Structure.mapToStructure;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.buildFlags;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.api.Client;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.events.EventDetails;
import dev.openfeature.api.exceptions.FlagNotFoundError;
import dev.openfeature.api.exceptions.ProviderNotReadyError;
import dev.openfeature.api.exceptions.TypeMismatchError;
import dev.openfeature.api.types.Value;
import dev.openfeature.sdk.OpenFeatureAPITestUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryProviderTest {

    private Client client;

    private InMemoryProvider provider;
    private OpenFeatureAPI api;

    @BeforeEach
    void beforeEach() throws Exception {
        final var configChangedEventCounter = new AtomicInteger();
        Map<String, Flag<?>> flags = buildFlags();
        provider = spy(new InMemoryProvider(flags));
        api = OpenFeatureAPITestUtil.createAPI();
        api.onProviderConfigurationChanged(eventDetails -> configChangedEventCounter.incrementAndGet());
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

        // wait for the two config changed events to be fired, otherwise they could mess with our tests
        while (configChangedEventCounter.get() < 2) {
            Thread.sleep(1);
        }
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
            provider.getBooleanEvaluation("not-found-flag", false, EvaluationContext.EMPTY);
        });
    }

    @Test
    void typeMismatch() {
        assertThrows(TypeMismatchError.class, () -> {
            provider.getBooleanEvaluation("string-flag", false, EvaluationContext.EMPTY);
        });
    }

    @Test
    void shouldThrowIfNotInitialized() throws Exception {
        InMemoryProvider inMemoryProvider = new InMemoryProvider(new HashMap<>());

        // ErrorCode.PROVIDER_NOT_READY should be returned when evaluated via the client
        assertThrows(
                ProviderNotReadyError.class,
                () -> inMemoryProvider.getBooleanEvaluation("fail_not_initialized", false, EvaluationContext.EMPTY));
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

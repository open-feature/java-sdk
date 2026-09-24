package dev.openfeature.sdk.providers.memory;

import static dev.openfeature.sdk.Structure.mapToStructure;
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
import java.util.concurrent.atomic.AtomicInteger;
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

    @Test
    void getLongEvaluation_nativeLongVariant() {
        InMemoryProvider local = new InMemoryProvider(Map.of(
                "long-flag",
                Flag.builder()
                        .variant("big", 9_007_199_254_740_991L)
                        .defaultVariant("big")
                        .build()));
        api.setProviderAndWait(local);
        assertEquals(9_007_199_254_740_991L, api.getClient().getLongValue("long-flag", 0L));
    }

    @Test
    void getLongEvaluation_widensIntegerVariantToLong() {
        InMemoryProvider local = new InMemoryProvider(Map.of(
                "int-as-long",
                Flag.builder().variant("v", 42).defaultVariant("v").build()));
        api.setProviderAndWait(local);
        assertEquals(42L, api.getClient().getLongValue("int-as-long", 0L));
    }

    @SneakyThrows
    @Test
    void getLongEvaluation_doesNotWidenDouble() {
        InMemoryProvider local = new InMemoryProvider(Map.of(
                "double-as-long",
                Flag.builder().variant("v", 42.0).defaultVariant("v").build()));
        local.initialize(new ImmutableContext());
        assertThrows(
                TypeMismatchError.class, () -> local.getLongEvaluation("double-as-long", 0L, new ImmutableContext()));
    }

    @SneakyThrows
    @Test
    void getIntegerEvaluation_doesNotAcceptLongVariant() {
        InMemoryProvider local = new InMemoryProvider(Map.of(
                "long-flag",
                Flag.builder().variant("v", 42L).defaultVariant("v").build()));
        local.initialize(new ImmutableContext());
        assertThrows(TypeMismatchError.class, () -> local.getIntegerEvaluation("long-flag", 0, new ImmutableContext()));
    }

    @SneakyThrows
    @Test
    void contextEvaluator_widensIntegerResultToLong() {
        Flag<Object> flag = Flag.<Object>builder()
                .variant("v", 0L)
                .defaultVariant("v")
                .contextEvaluator((f, ctx) -> Integer.valueOf(42))
                .build();
        InMemoryProvider local = new InMemoryProvider(Map.of("targeted", flag));
        local.initialize(new ImmutableContext());
        assertEquals(
                42L,
                local.getLongEvaluation("targeted", 0L, new ImmutableContext()).getValue());
    }

    @SneakyThrows
    @Test
    void contextEvaluator_rejectsMismatchedResultType() {
        Flag<Object> flag = Flag.<Object>builder()
                .variant("v", 0L)
                .defaultVariant("v")
                .contextEvaluator((f, ctx) -> Double.valueOf(3.14))
                .build();
        InMemoryProvider local = new InMemoryProvider(Map.of("targeted", flag));
        local.initialize(new ImmutableContext());
        assertThrows(TypeMismatchError.class, () -> local.getLongEvaluation("targeted", 0L, new ImmutableContext()));
    }

    @SneakyThrows
    @Test
    void contextEvaluator_nullResultFallsBackToDefaultVariantWithTypeCheck() {
        Flag<Object> flag = Flag.<Object>builder()
                .variant("v", 7L)
                .defaultVariant("v")
                .contextEvaluator((f, ctx) -> null)
                .build();
        InMemoryProvider local = new InMemoryProvider(Map.of("targeted", flag));
        local.initialize(new ImmutableContext());
        assertEquals(
                7L,
                local.getLongEvaluation("targeted", 0L, new ImmutableContext()).getValue());
    }
}

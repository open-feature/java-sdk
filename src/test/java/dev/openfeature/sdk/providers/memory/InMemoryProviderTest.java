package dev.openfeature.sdk.providers.memory;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.sdk.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.openfeature.sdk.providers.memory.InMemoryProvider.mapToStructure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryProviderTest {

    private static Client client;

    private static final AtomicBoolean isProviderReady = new AtomicBoolean(false);

    private static final AtomicBoolean isConfigurationChanged = new AtomicBoolean(false);

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        Flags flags = Flags.builder()
            .flag("boolean-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("on", true)
                .variant("off", false)
                .defaultVariant("on")
                .build())
            .flag("string-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("greeting", "hi")
                .variant("parting", "bye")
                .defaultVariant("greeting")
                .build())
            .flag("integer-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("one", 1)
                .variant("ten", 10)
                .defaultVariant("ten")
                .build())
            .flag("float-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("tenth", 0.1)
                .variant("half", 0.5)
                .defaultVariant("half")
                .build())
            .flag("object-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("empty", new HashMap<>())
                .variant("template", new Value(mapToStructure(ImmutableMap.of(
                    "showImages", new Value(true),
                    "title", new Value("Check out these pics!"),
                    "imagesPerPage", new Value(100)
                ))))
                .defaultVariant("template")
                .build())
            .flag("context-aware", Flag.<String>builder().state(Flag.State.ENABLED)
                .variant("internal", "INTERNAL")
                .variant("external", "EXTERNAL")
                .defaultVariant("external")
                .contextEvaluator((flag, evaluationContext) -> {
                    if (new Value(false).equals(evaluationContext.getValue("customer"))) {
                        return (String) flag.getVariants().get("internal");
                    } else {
                        return (String) flag.getVariants().get(flag.getDefaultVariant());
                    }
                })
                .build())
            .flag("wrong-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("one", "uno")
                .variant("two", "dos")
                .defaultVariant("one")
                .build())
            .build();
        InMemoryProvider provider = new InMemoryProvider(flags);
        OpenFeatureAPI.getInstance().setProvider(provider);

        // TODO: setProvider with wait for init, pending https://github.com/open-feature/ofep/pull/80
        Thread.sleep(500);

        client = OpenFeatureAPI.getInstance().getClient();

        client.onProviderReady(eventDetails -> isProviderReady.set(true));

        client.onProviderConfigurationChanged(eventDetails -> isConfigurationChanged.set(true));
        provider.updateFlags(flags);
    }

    @SneakyThrows
    @Test
    void eventsTest() {
        assertTrue(isProviderReady.get());
        assertTrue(isConfigurationChanged.get());
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

    @SneakyThrows
    @Test
    void mapToStructureTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("String", "str");
        map.put("Boolean", true);
        map.put("Integer", 1);
        map.put("Double", 1.1);
        map.put("List", Collections.singletonList(new Value(1)));
        map.put("Value", new Value((true)));
        map.put("Instant", Instant.ofEpochSecond(0));
        map.put("Map", new HashMap<>());
        ImmutableContext immutableContext = new ImmutableContext();
        map.put("ImmutableContext", immutableContext);
        Structure res = mapToStructure(map);
        assertEquals(new Value("str"), res.getValue("String"));
        assertEquals(new Value(true), res.getValue("Boolean"));
        assertEquals(new Value(1), res.getValue("Integer"));
        assertEquals(new Value(1.1), res.getValue("Double"));
        assertEquals(new Value(Collections.singletonList(new Value(1))), res.getValue("List"));
        assertEquals(new Value(true), res.getValue("Value"));
        assertEquals(new Value(Instant.ofEpochSecond(0)), res.getValue("Instant"));
        assertEquals(new HashMap<>(), res.getValue("Map").asStructure().asMap());
        assertEquals(new Value(immutableContext), res.getValue("ImmutableContext"));
    }

}
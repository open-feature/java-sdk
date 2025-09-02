package dev.openfeature.sdk.vmlens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vmlens.api.AllInterleavings;
import com.vmlens.api.Runner;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.OpenFeatureAPITestUtil;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VmLensTest {
    final OpenFeatureAPI api = OpenFeatureAPITestUtil.createAPI();

    @BeforeEach
    void setUp() {
        var flags = new HashMap<String, Flag<?>>();
        flags.put("a", Flag.builder().variant("a", "def").defaultVariant("a").build());
        flags.put("b", Flag.builder().variant("a", "as").defaultVariant("a").build());
        api.setProviderAndWait(new InMemoryProvider(flags));
    }

    @AfterEach
    void tearDown() {
        api.clearHooks();
        api.shutdown();
    }

    @Test
    void concurrentClientCreations() {
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent creations of the Client")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(api::getClient, api::getClient);
            }
        }
        // keep the linter happy
        assertTrue(true);
    }

    @Test
    void concurrentFlagEvaluations() {
        var client = api.getClient();
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent evaluations")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> assertEquals("as", client.getStringValue("b", "b")));
            }
        }
    }

    @Test
    void concurrentContextSetting() {
        var client = api.getClient();
        var contextA = new ImmutableContext(Map.of("a", new Value("b")));
        var contextB = new ImmutableContext(Map.of("c", new Value("d")));
        try (AllInterleavings allInterleavings =
                new AllInterleavings("Concurrently setting the context and evaluating a flag")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> client.setEvaluationContext(contextA),
                        () -> client.setEvaluationContext(contextB));
                assertThat(client.getEvaluationContext()).isIn(contextA, contextB);
            }
        }
    }
}

package dev.openfeature.sdk;

import com.vmlens.api.AllInterleavings;
import com.vmlens.api.Runner;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Javadoc.
 */
@EnabledIfSystemProperty(named = "--activate-profiles", matches = "vmlens")
class VmLensTest {
    final OpenFeatureAPI api = new OpenFeatureAPI();

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
    void fail(){
        assertTrue(false);
    }

    @Test
    void concurrentFlagEvaluations() {
        var client = api.getClient();
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent evaluations")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> assertEquals("as", client.getStringValue("b", "b"))
                );
            }
        }
    }

    @Test
    void concurrentFlagEvaluationsAndHookAdditions() {
        var client = api.getClient();
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent evaluations and hook additions")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> client.addHooks(new StringHook() {})
                );
            }
        }
    }
// todo add tests:
    // concurrent changing of context thorugh client.setctx... and flags with a targeting rule depending on that context
    // concurrent setting of context thorugh client.setctx... and flags with a targeting rule depending on that context
    // concurrent changing of context through a hook and flags with a targeting rule depending on that context
    // concurrent setting of context through a hook and flags with a targeting rule depending on that context

    @Test
    void concurrentClientCreations() {
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent creations of the Client")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(
                        api::getClient,
                        api::getClient
                );
            }
        }
        // keep the linter happy
        assertTrue(true);
    }

    @Test
    void concurrentContextSetting() {
        var client = api.getClient();
        try (AllInterleavings allInterleavings = new AllInterleavings(
                "Concurrently setting the context and evaluating a flag")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> client.setEvaluationContext(new ImmutableContext(Map.of("a", new Value("b")))),
                        () -> client.setEvaluationContext(new ImmutableContext(Map.of("c", new Value("d"))))
                );
            }
        }
    }
}

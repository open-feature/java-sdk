package dev.openfeature.sdk.vmlens;

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

/**
 * Javadoc.
 */
class VmLensTest {
    final OpenFeatureAPI api = OpenFeatureAPITestUtil.createAPI();

    // todo add tests:
    // concurrent changing of context thorugh client.setctx... and flags with a targeting rule depending on that context
    // concurrent setting of context thorugh client.setctx... and flags with a targeting rule depending on that context
    // concurrent changing of context through a hook and flags with a targeting rule depending on that context
    // concurrent setting of context through a hook and flags with a targeting rule depending on that context

    @BeforeEach
    void setUp() {
        System.out.println("VmLensTest.setUp");
        var flags = new HashMap<String, Flag<?>>();
        flags.put("a", Flag.builder().variant("a", "def").defaultVariant("a").build());
        flags.put("b", Flag.builder().variant("a", "as").defaultVariant("a").build());
        api.setProviderAndWait(new InMemoryProvider(flags));
    }

    @AfterEach
    void tearDown() {
        System.out.println("VmLensTest.tearDown");
        api.clearHooks();
        api.shutdown();
    }

    @Test
    void concurrentClientCreations() {
        System.out.println("VmLensTest.concurrentClientCreations");
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent creations of the Client")) {
            while (allInterleavings.hasNext()) {
                System.out.println("iteration");
                Runner.runParallel(api::getClient, api::getClient);
            }
        }
        // keep the linter happy
        assertTrue(true);
    }

    @Test
    void concurrentFlagEvaluations() {
        System.out.println("VmLensTest.concurrentFlagEvaluations");
        var client = api.getClient();
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent evaluations")) {
            while (allInterleavings.hasNext()) {
                System.out.println("iteration");
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> assertEquals("as", client.getStringValue("b", "b")));
            }
        }
    }

    @Test
    void concurrentContextSetting() {
        System.out.println("VmLensTest.concurrentContextSetting");
        var client = api.getClient();
        try (AllInterleavings allInterleavings =
                new AllInterleavings("Concurrently setting the context and evaluating a flag")) {
            while (allInterleavings.hasNext()) {
                System.out.println("iteration");
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> client.setEvaluationContext(new ImmutableContext(Map.of("a", new Value("b")))),
                        () -> client.setEvaluationContext(new ImmutableContext(Map.of("c", new Value("d")))));
            }
        }
    }
}

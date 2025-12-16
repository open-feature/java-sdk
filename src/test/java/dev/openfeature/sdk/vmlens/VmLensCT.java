package dev.openfeature.sdk.vmlens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vmlens.api.AllInterleavings;
import com.vmlens.api.Runner;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.OpenFeatureAPITestUtil;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VmLensCT {
    private OpenFeatureAPI api;
    private Client client;

    @BeforeEach
    void setUp() {
        api = OpenFeatureAPITestUtil.createAPI();
        var flags = new HashMap<String, Flag<?>>();
        flags.put("a", Flag.builder().variant("a", "def").defaultVariant("a").build());
        flags.put("b", Flag.builder().variant("a", "as").defaultVariant("a").build());
        api.setProviderAndWait(new InMemoryProvider(flags));
        client = api.getClient();
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
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent evaluations")) {
            while (allInterleavings.hasNext()) {
                Runner.runParallel(
                        () -> assertEquals("def", client.getStringValue("a", "a")),
                        () -> assertEquals("as", client.getStringValue("b", "b")));
            }
        }
    }

    @Nested
    class ConcurrentContext {
        private final ImmutableContext contextA = new ImmutableContext(Map.of("a", new Value("b")));
        private final ImmutableContext contextB = new ImmutableContext(Map.of("c", new Value("d")));

        @Test
        void concurrentContextSetting() {
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

    @Nested
    class ConcurrentHooks {
        private final Hook hook0 = new Hook<>() {};
        private final Hook hook1 = new Hook<>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Object> ctx, Map<String, Object> hints) {
                return Optional.of(new ImmutableContext(Map.of("c", new Value("d"))));
            }
        };

        @Test
        void concurrentAdditionOfHooksToClient() {
            try (AllInterleavings allInterleavings =
                    new AllInterleavings("Concurrently adding client hooks and evaluating a flag")) {
                while (allInterleavings.hasNext()) {
                    Runner.runParallel(
                            () -> assertEquals("def", client.getStringValue("a", "a")),
                            () -> client.addHooks(hook0),
                            () -> client.addHooks(hook1));
                    assertThat(client.getHooks()).containsAll(List.of(hook0, hook1));
                }
            }
        }

        @Test
        void concurrentAdditionOfHooksToApi() {
            try (AllInterleavings allInterleavings =
                    new AllInterleavings("Concurrently adding api hooks and evaluating a flag")) {
                while (allInterleavings.hasNext()) {
                    Runner.runParallel(
                            () -> assertEquals("def", client.getStringValue("a", "a")),
                            () -> api.addHooks(hook0),
                            () -> api.addHooks(hook1));
                    assertThat(api.getHooks()).containsAll(List.of(hook0, hook1));
                }
            }
        }

        @Test
        void concurrentAdditionOfHooksToApiAndClient() {
            try (AllInterleavings allInterleavings =
                    new AllInterleavings("Concurrently adding api and client hooks and evaluating a flag")) {
                while (allInterleavings.hasNext()) {
                    Runner.runParallel(
                            () -> assertEquals("def", client.getStringValue("a", "a")),
                            () -> api.addHooks(hook0),
                            () -> client.addHooks(hook1));
                    assertThat(api.getHooks()).containsAll(List.of(hook0));
                    assertThat(client.getHooks()).containsAll(List.of(hook1));
                }
            }
        }
    }
}

package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LayeredEvaluationContextTest {
    final EvaluationContext apiContext =
            new MutableContext("api-level", Map.of("api", new Value("api"), "override", new Value("api")));
    final EvaluationContext transactionContext = new MutableContext(
            "transaction-level", Map.of("transaction", new Value("transaction"), "override", new Value("transaction")));
    final EvaluationContext clientContext =
            new MutableContext("client-level", Map.of("client", new Value("client"), "override", new Value("client")));
    final EvaluationContext invocationContext = new MutableContext(
            "invocation-level", Map.of("invocation", new Value("invocation"), "override", new Value("invocation")));
    final EvaluationContext hookContext =
            new MutableContext("hook-level", Map.of("hook", new Value("hook"), "override", new Value("hook")));

    @Test
    void creatingLayeredContextWithNullsWorks() {
        LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
        assertNotNull(layeredContext);
        assertNull(layeredContext.getTargetingKey());
        assertEquals(Map.of(), layeredContext.asMap());
        assertEquals(Map.of(), layeredContext.asObjectMap());
        assertEquals(Map.of(), layeredContext.asUnmodifiableMap());
        assertEquals(Set.of(), layeredContext.keySet());
        assertTrue(layeredContext.isEmpty());
    }

    @Test
    void addingNullHookWorks() {
        LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
        assertDoesNotThrow(() -> layeredContext.putHookContext(null));
    }

    @Nested
    class TargetingKey {
        @Test
        void hookWins() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);
            assertEquals("hook-level", layeredContext.getTargetingKey());
        }

        @Test
        void hookWithoutTargetingKeyDoesNotChangeIt() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(
                    new MutableContext(Map.of("hook", new Value("hook"), "override", new Value("hook"))));
            assertEquals("invocation-level", layeredContext.getTargetingKey());
        }

        @Test
        void invocationWinsIfHookNotSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            assertEquals("invocation-level", layeredContext.getTargetingKey());
        }

        @Test
        void clientWinsIfInvocationAndHookNotSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, null);
            assertEquals("client-level", layeredContext.getTargetingKey());
        }

        @Test
        void transactionWinsIfClientInvocationAndHookNotSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, null, null);
            assertEquals("transaction-level", layeredContext.getTargetingKey());
        }

        @Test
        void apiWinsIfTransactionClientInvocationAndHookNotSet() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(apiContext, null, null, null);
            assertEquals("api-level", layeredContext.getTargetingKey());
        }
    }

    @Nested
    class GetValue {
        @Test
        void doesNotOverrideUniqueValues() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);

            assertEquals("hook", layeredContext.getValue("hook").asString());
            assertEquals("invocation", layeredContext.getValue("invocation").asString());
            assertEquals("client", layeredContext.getValue("client").asString());
            assertEquals("transaction", layeredContext.getValue("transaction").asString());
            assertEquals("api", layeredContext.getValue("api").asString());
        }

        @Test
        void hookWins() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);
            assertEquals("hook", layeredContext.getValue("override").asString());
        }

        @Test
        void invocationWinsIfHookNotSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            assertEquals("invocation", layeredContext.getValue("override").asString());
        }

        @Test
        void clientWinsIfInvocationAndHookNotSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, null);
            assertEquals("client", layeredContext.getValue("override").asString());
        }

        @Test
        void transactionWinsIfClientInvocationAndHookNotSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, null, null);
            assertEquals("transaction", layeredContext.getValue("override").asString());
        }

        @Test
        void apiWinsIfTransactionClientInvocationAndHookNotSet() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(apiContext, null, null, null);
            assertEquals("api", layeredContext.getValue("override").asString());
        }
    }

    @Nested
    class KeySet {
        @Test
        void keySetIsGeneratedCorrectly() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);

            Set<String> expectedKeys = Set.of(
                    "hook",
                    "invocation",
                    "client",
                    "transaction",
                    "api",
                    "override",
                    "targetingKey" // expected, even though not explicitly set
                    );

            assertEquals(expectedKeys, layeredContext.keySet());
        }

        @Test
        void keySetIsUpdatedWhenHookContextChanges() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);

            Set<String> expectedKeys =
                    Set.of("hook", "invocation", "client", "transaction", "api", "override", "targetingKey");
            assertEquals(expectedKeys, layeredContext.keySet());

            layeredContext.putHookContext(new ImmutableContext(Map.of("new", new Value("hook"))));

            expectedKeys =
                    Set.of("hook", "invocation", "client", "transaction", "api", "override", "targetingKey", "new");
            assertEquals(expectedKeys, layeredContext.keySet());
        }
    }

    @Nested
    class AsMap {
        @Test
        void mapIsGeneratedCorrectly() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);

            var expectedKeys = Map.of(
                    "hook", new Value("hook"),
                    "invocation", new Value("invocation"),
                    "client", new Value("client"),
                    "transaction", new Value("transaction"),
                    "api", new Value("api"),
                    "override", new Value("hook"),
                    "targetingKey", new Value("hook-level") // expected, even though not explicitly set
                    );

            assertEquals(expectedKeys, layeredContext.asMap());
            assertEquals(expectedKeys, layeredContext.asUnmodifiableMap());
        }

        @Test
        void emptyContextGeneratesEmptyMap() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
            assertEquals(Map.of(), layeredContext.asMap());
            assertEquals(Map.of(), layeredContext.asUnmodifiableMap());
            assertEquals(Map.of(), layeredContext.asObjectMap());
        }
    }

    @Nested
    class AsObjectMap {
        @Test
        void mapIsGeneratedCorrectly() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);

            var expectedKeys = Map.of(
                    "hook", "hook",
                    "invocation", "invocation",
                    "client", "client",
                    "transaction", "transaction",
                    "api", "api",
                    "override", "hook",
                    "targetingKey", "hook-level" // expected, even though not explicitly set in map
                    );

            assertEquals(expectedKeys, layeredContext.asObjectMap());
        }
    }

    @Nested
    class IsEmpty {
        @Test
        void isEmptyWhenAllContextsAreNull() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
            assertTrue(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenApiContextIsSet() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(apiContext, null, null, null);
            assertFalse(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenTransactionContextIsSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(null, transactionContext, null, null);
            assertFalse(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenClientContextIsSet() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, clientContext, null);
            assertFalse(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenInvocationContextIsSet() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, invocationContext);
            assertFalse(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenInvocationAndClientContextIsSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(null, null, clientContext, invocationContext);
            assertFalse(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenInvocationAndClientAndTransactionContextIsSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(null, transactionContext, clientContext, invocationContext);
            assertFalse(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenInvocationAndClientAndTransactionAndApiContextIsSet() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            assertFalse(layeredContext.isEmpty());
        }

        @Test
        void isNotEmptyWhenHookContextIsSet() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
            layeredContext.putHookContext(hookContext);
            assertFalse(layeredContext.isEmpty());
        }
    }

    @Nested
    class Merge {
        @Test
        void mergesCorrectly() {
            LayeredEvaluationContext ctx1 =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            EvaluationContext ctx2 = new MutableContext(
                    "mutable", Map.of("override", new Value("other"), "unique", new Value("unique")));

            EvaluationContext merged = ctx1.merge(ctx2);

            assertEquals(
                    Map.of(
                            "invocation", new Value("invocation"),
                            "client", new Value("client"),
                            "transaction", new Value("transaction"),
                            "api", new Value("api"),
                            "override", new Value("other"),
                            "targetingKey", new Value("mutable"),
                            "unique", new Value("unique")),
                    merged.asMap());
            assertEquals("mutable", merged.getTargetingKey());
        }

        @Test
        void mergesCorrectlyWithHooks() {
            LayeredEvaluationContext ctx1 =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            ctx1.putHookContext(hookContext);
            EvaluationContext ctx2 = new MutableContext(
                    "mutable", Map.of("override", new Value("other"), "unique", new Value("unique")));

            EvaluationContext merged = ctx1.merge(ctx2);

            assertEquals(
                    Map.of(
                            "invocation", new Value("invocation"),
                            "client", new Value("client"),
                            "transaction", new Value("transaction"),
                            "api", new Value("api"),
                            "override", new Value("other"),
                            "targetingKey", new Value("mutable"),
                            "unique", new Value("unique"),
                            "hook", new Value("hook")),
                    merged.asMap());
            assertEquals("mutable", merged.getTargetingKey());
        }

        @Test
        void mergesCorrectlyWhenOtherHasNoTargetingKey() {
            LayeredEvaluationContext ctx1 =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            EvaluationContext ctx2 =
                    new MutableContext(Map.of("override", new Value("other"), "unique", new Value("unique")));

            EvaluationContext merged = ctx1.merge(ctx2);

            assertEquals(
                    Map.of(
                            "invocation", new Value("invocation"),
                            "client", new Value("client"),
                            "transaction", new Value("transaction"),
                            "api", new Value("api"),
                            "override", new Value("other"),
                            "targetingKey", new Value(invocationContext.getTargetingKey()),
                            "unique", new Value("unique")),
                    merged.asMap());
            assertEquals(invocationContext.getTargetingKey(), merged.getTargetingKey());
        }
    }
}

package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            // cached key set
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

        @Test
        void creatingMapWithCachedEmptyKeySetWorks() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
            assertNotNull(layeredContext.keySet());
            assertEquals(Map.of(), layeredContext.asMap());
            assertEquals(Map.of(), layeredContext.asUnmodifiableMap());
        }

        @Test
        void creatingMapWithCachedNonEmptyKeySetWorks() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(apiContext, null, null, null);
            assertNotNull(layeredContext.keySet());
            assertEquals(apiContext.asMap(), layeredContext.asMap());
            assertEquals(apiContext.asUnmodifiableMap(), layeredContext.asUnmodifiableMap());
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

        @Test
        void creatingMapWithCachedEmptyKeySetWorks() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
            assertNotNull(layeredContext.keySet());
            assertEquals(Map.of(), layeredContext.asObjectMap());
        }

        @Test
        void creatingMapWithCachedNonEmptyKeySetWorks() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(apiContext, null, null, null);
            assertNotNull(layeredContext.keySet());
            assertEquals(apiContext.asObjectMap(), layeredContext.asObjectMap());
        }

        @Test
        void nestedContextsAreUnwrappedCorrectly() {
            var innerApiContext = new ImmutableContext(Map.of("inner", new Value("api")));
            var outerApiContext = new ImmutableContext(Map.of("outer", new Value(innerApiContext)));

            var innerClientContext = new ImmutableContext(Map.of("inner", new Value("client")));
            var outerClientContext = new ImmutableContext(Map.of("outer", new Value(innerClientContext)));
            var layeredContext = new LayeredEvaluationContext(outerApiContext, null, outerClientContext, null);

            var objectMap = layeredContext.asObjectMap();

            assertEquals(Map.of("outer", Map.of("inner", "client")), objectMap);
        }

        @Test
        void nestedStructuresInContextsAreUnwrappedCorrectly() {
            var innerApiStructure = new ImmutableStructure(Map.of("inner", new Value("api")));
            var outerApiContext = new ImmutableContext(Map.of("outer", new Value(innerApiStructure)));

            var innerClientStructure = new ImmutableStructure(Map.of("inner", new Value("client")));
            var outerClientContext = new ImmutableContext(Map.of("outer", new Value(innerClientStructure)));
            var layeredContext = new LayeredEvaluationContext(outerApiContext, null, outerClientContext, null);

            var objectMap = layeredContext.asObjectMap();

            assertEquals(Map.of("outer", Map.of("inner", "client")), objectMap);
        }

        @Test
        void nestedHookContextsAreUnwrappedCorrectly() {
            var innerApiStructure = new ImmutableStructure(Map.of("inner", new Value("api")));
            var outerApiContext = new ImmutableContext(Map.of("outer", new Value(innerApiStructure)));

            var innerClientStructure = new ImmutableStructure(Map.of("inner", new Value("client")));
            var outerClientContext = new ImmutableContext(Map.of("outer", new Value(innerClientStructure)));
            var layeredContext = new LayeredEvaluationContext(outerApiContext, null, outerClientContext, null);

            var innerHookStructure = new ImmutableStructure(Map.of("inner", new Value("hook")));
            var outerHookContext = new ImmutableContext(Map.of("outer", new Value(innerHookStructure)));

            layeredContext.putHookContext(outerHookContext);

            var objectMap = layeredContext.asObjectMap();

            assertEquals(Map.of("outer", Map.of("inner", "hook")), objectMap);
        }

        @Test
        void objectMapIsMutable() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);

            var objectMap = layeredContext.asObjectMap();
            assertDoesNotThrow(() -> objectMap.put("a", "b"));
            assertEquals("b", objectMap.get("a"));
        }

        @Test
        void mutatingObjectMapHasNoSideEffects() {
            LayeredEvaluationContext layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);

            var objectMap1 = layeredContext.asObjectMap();
            objectMap1.put("a", "b");

            var objectMap2 = layeredContext.asObjectMap();
            assertNull(objectMap2.get("a"));
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

        @Test
        void isEmptyIfHookContextIsEmpty() {
            LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(null, null, null, null);
            layeredContext.putHookContext(new MutableContext());
            assertTrue(layeredContext.isEmpty());
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

        @Test
        void testLayeredContextEquality() {
            Map<String, Value> baseMap = Map.of("k", new Value("v"));
            Map<String, Value> layerMap = Map.of("x", new Value("y"));

            EvaluationContext base = new MutableContext(null, baseMap);
            EvaluationContext layer = new MutableContext(null, layerMap);

            LayeredEvaluationContext l1 = new LayeredEvaluationContext(base, layer, null, null);
            LayeredEvaluationContext l2 = new LayeredEvaluationContext(base, layer, null, null);

            assertEquals(l1, l2);
            assertEquals(l1.hashCode(), l2.hashCode());
        }

        @Test
        void testMixedContextEquality() {
            Map<String, Value> map = Map.of("foo", new Value("bar"));

            EvaluationContext base = new MutableContext(null, map);
            LayeredEvaluationContext layered = new LayeredEvaluationContext(null, null, null, base);

            // Equality from the layered context's perspective (map-based equality)
            assertEquals(layered, base);

            // Resolved maps should be identical
            assertEquals(base.asMap(), layered.asMap());

            // Layered's hashCode must be consistent with its resolved attribute map
            assertEquals(base.asMap().hashCode(), layered.hashCode());
        }
    }

    @Nested
    class Equals {
        @Test
        void equalsItself() {
            var layeredContext =
                    new LayeredEvaluationContext(apiContext, transactionContext, clientContext, invocationContext);
            layeredContext.putHookContext(hookContext);
            assertEquals(layeredContext, layeredContext);
        }

        @Test
        void equalsDifferentLayeredEvalCtxIfSameValues() {
            var layeredContext1 = new LayeredEvaluationContext(apiContext, null, null, null);
            var layeredContext2 = new LayeredEvaluationContext(null, apiContext, null, null);
            assertEquals(layeredContext1, layeredContext2);
        }

        @Test
        void equalsDifferentImmutableEvalCtxIfSameValues() {
            var immutable = new ImmutableContext("key", Map.of("prop", new Value("erty")));
            var layeredContext = new LayeredEvaluationContext(immutable, null, null, null);
            assertEquals(immutable, layeredContext);
            assertEquals(layeredContext, immutable);
        }

        @Test
        void equalsDifferentMutableEvalCtxIfSameValues() {
            var mutable = new MutableContext("key", Map.of("prop", new Value("erty")));
            var layeredContext = new LayeredEvaluationContext(mutable, null, null, null);
            assertEquals(mutable, layeredContext);
            assertEquals(layeredContext, mutable);
        }
    }

    @Nested
    class HashCode {
        ImmutableContext immutable = new ImmutableContext("c", Map.of("a", new Value("b")));
        LayeredEvaluationContext layeredContext = new LayeredEvaluationContext(immutable, null, null, null);

        @Test
        void hashCodeEqualsItself() {
            var layeredContext2 = new LayeredEvaluationContext(null, null, immutable, null);
            assertEquals(layeredContext.hashCode(), layeredContext2.hashCode());
        }

        @Test
        void hasSameHashCodeAsImmutableEvalCtxIfSameValues() {
            assertEquals(immutable.hashCode(), layeredContext.hashCode());
        }

        @Test
        void hashCodeEqualsDifferentMutableEvalCtxIfSameValues() {
            MutableContext ctx = new MutableContext("c", Map.of("a", new Value("b")));
            assertEquals(immutable.hashCode(), ctx.hashCode());
        }
    }
}

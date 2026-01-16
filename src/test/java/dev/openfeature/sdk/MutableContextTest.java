package dev.openfeature.sdk;

import static dev.openfeature.sdk.EvaluationContext.TARGETING_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MutableContextTest {

    @DisplayName("attributes unable to allow mutation should not affect the Mutable context")
    @Test
    void shouldNotAttemptToModifyAttributesForMutableContext() {
        final Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        // should check the usage of Map.of() which is a more likely use case, but that API isn't available in Java 8
        EvaluationContext ctx = new MutableContext("targeting key", Collections.unmodifiableMap(attributes));
        attributes.put("key3", new Value("val3"));
        assertArrayEquals(
                new Object[] {"key1", "key2", TARGETING_KEY}, ctx.keySet().toArray());
    }

    @DisplayName("targeting key should be changed from the overriding context")
    @Test
    void shouldChangeTargetingKeyFromOverridingContext() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new MutableContext("targeting key", attributes);
        EvaluationContext overriding = new MutableContext("overriding_key");
        EvaluationContext merge = ctx.merge(overriding);
        assertEquals("overriding_key", merge.getTargetingKey());
    }

    @DisplayName("targeting key should be changed from the overriding context even if empty string")
    @Test
    void shouldOverrideTargetingKeyWhenOverridingContextTargetingKeyIsEmptyString() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new MutableContext("targeting_key", attributes);
        EvaluationContext overriding = new MutableContext("");
        EvaluationContext merge = ctx.merge(overriding);
        // Empty string is a valid targeting key and should override
        assertEquals("", merge.getTargetingKey());
    }

    @DisplayName("missing targeting key should return null")
    @Test
    void missingTargetingKeyShould() {
        EvaluationContext ctx = new MutableContext();
        assertEquals(null, ctx.getTargetingKey());
    }

    @DisplayName("empty string is a valid targeting key via constructor")
    @Test
    void emptyStringIsValidTargetingKeyViaConstructor() {
        EvaluationContext ctx = new MutableContext("");
        assertEquals("", ctx.getTargetingKey());
    }

    @DisplayName("empty and whitespace-only strings are valid targeting keys via setter")
    @Test
    void emptyAndWhitespaceAreValidTargetingKeysViaSetter() {
        MutableContext ctx = new MutableContext();

        ctx.setTargetingKey("");
        assertEquals("", ctx.getTargetingKey());

        ctx.setTargetingKey("   ");
        assertEquals("   ", ctx.getTargetingKey());
    }

    @DisplayName("whitespace-only string is a valid targeting key via constructor")
    @Test
    void whitespaceOnlyStringIsValidTargetingKeyViaConstructor() {
        EvaluationContext ctx = new MutableContext("   ");
        assertEquals("   ", ctx.getTargetingKey());
    }

    @DisplayName("Merge should retain all the attributes from the existing context when overriding context is null")
    @Test
    void mergeShouldReturnAllTheValuesFromTheContextWhenOverridingContextIsNull() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new MutableContext("targeting_key", attributes);
        EvaluationContext merge = ctx.merge(null);
        assertEquals("targeting_key", merge.getTargetingKey());
        assertArrayEquals(
                new Object[] {"key1", "key2", TARGETING_KEY}, merge.keySet().toArray());
    }

    @DisplayName(
            "Merge should retain subkeys from the existing context when the overriding context has the same targeting key")
    @Test
    void mergeShouldRetainItsSubkeysWhenOverridingContextHasTheSameKey() {
        HashMap<String, Value> attributes = new HashMap<>();
        HashMap<String, Value> overridingAttributes = new HashMap<>();
        HashMap<String, Value> key1Attributes = new HashMap<>();
        HashMap<String, Value> ovKey1Attributes = new HashMap<>();

        key1Attributes.put("key1_1", new Value("val1_1"));
        attributes.put("key1", new Value(new ImmutableStructure(key1Attributes)));
        attributes.put("key2", new Value("val2"));
        ovKey1Attributes.put("overriding_key1_1", new Value("overriding_val_1_1"));
        overridingAttributes.put("key1", new Value(new ImmutableStructure(ovKey1Attributes)));

        EvaluationContext ctx = new MutableContext("targeting_key", attributes);
        EvaluationContext overriding = new MutableContext("targeting_key", overridingAttributes);
        EvaluationContext merge = ctx.merge(overriding);
        assertEquals("targeting_key", merge.getTargetingKey());
        assertArrayEquals(
                new Object[] {"key1", "key2", TARGETING_KEY}, merge.keySet().toArray());

        Value key1 = merge.getValue("key1");
        assertTrue(key1.isStructure());

        Structure value = key1.asStructure();
        assertArrayEquals(
                new Object[] {"key1_1", "overriding_key1_1"}, value.keySet().toArray());
    }

    @DisplayName(
            "Merge should retain subkeys from the existing context when the overriding context doesn't have targeting key")
    @Test
    void mergeShouldRetainItsSubkeysWhenOverridingContextHasNoTargetingKey() {
        HashMap<String, Value> attributes = new HashMap<>();
        HashMap<String, Value> key1Attributes = new HashMap<>();

        key1Attributes.put("key1_1", new Value("val1_1"));
        attributes.put("key1", new Value(new ImmutableStructure(key1Attributes)));
        attributes.put("key2", new Value("val2"));

        EvaluationContext ctx = new MutableContext(attributes);
        EvaluationContext overriding = new MutableContext();
        EvaluationContext merge = ctx.merge(overriding);
        assertArrayEquals(new Object[] {"key1", "key2"}, merge.keySet().toArray());

        Value key1 = merge.getValue("key1");
        assertTrue(key1.isStructure());

        Structure value = key1.asStructure();
        assertArrayEquals(new Object[] {"key1_1"}, value.keySet().toArray());
    }

    @DisplayName("Ensure mutations are chainable")
    @Test
    void shouldAllowChainingOfMutations() {
        MutableContext context = new MutableContext();
        context.add("key1", "val1")
                .add("key2", 2)
                .setTargetingKey("TARGETING_KEY")
                .add("key3", 3.0);

        assertEquals("TARGETING_KEY", context.getTargetingKey());
        assertEquals("val1", context.getValue("key1").asString());
        assertEquals(2, context.getValue("key2").asInteger());
        assertEquals(3.0, context.getValue("key3").asDouble());
    }

    @Nested
    class Equals {
        MutableContext ctx = new MutableContext("c", Map.of("a", new Value("b")));

        @Test
        void equalsItself() {
            assertEquals(ctx, ctx);
        }

        @Test
        void equalsLayeredEvalCtxIfSameValues() {
            var layeredContext = new LayeredEvaluationContext(ctx, null, null, null);
            assertEquals(layeredContext, ctx);
            assertEquals(ctx, layeredContext);
        }

        @Test
        void equalsDifferentMutableEvalCtxIfSameValues() {
            var immutable = new ImmutableContext("c", Map.of("a", new Value("b")));
            assertEquals(immutable, ctx);
            assertEquals(ctx, immutable);
        }

        @DisplayName("Two different MutableContext objects with the different contents are not considered equal")
        @Test
        void unequalMutableContextsAreNotEqual() {
            final Map<String, Value> attributes = new HashMap<>();
            attributes.put("key1", new Value("val1"));
            final MutableContext context = new MutableContext(attributes);

            final Map<String, Value> attributes2 = new HashMap<>();
            final MutableContext ctx2 = new MutableContext(attributes2);

            assertNotEquals(context, ctx2);
        }

        @DisplayName("Two different MutableContext objects with the same content are considered equal")
        @Test
        void equalMutableContextsAreEqual() {
            final Map<String, Value> attributes = new HashMap<>();
            attributes.put("key1", new Value("val1"));
            final MutableContext context = new MutableContext(attributes);

            final Map<String, Value> attributes2 = new HashMap<>();
            attributes2.put("key1", new Value("val1"));
            final MutableContext ctx2 = new MutableContext(attributes2);

            assertEquals(context, ctx2);
        }
    }

    @Nested
    class HashCode {
        MutableContext ctx = new MutableContext("c", Map.of("a", new Value("b")));

        @Test
        void hashCodeEqualsLayeredEvalCtxIfSameValues() {
            var layeredContext = new LayeredEvaluationContext(ctx, null, null, null);
            assertEquals(layeredContext.hashCode(), ctx.hashCode());
        }

        @Test
        void hashCodeEqualsDifferentMutableEvalCtxIfSameValues() {
            var immutable = new ImmutableContext("c", Map.of("a", new Value("b")));
            assertEquals(immutable.hashCode(), ctx.hashCode());
        }
    }
}

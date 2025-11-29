package dev.openfeature.sdk;

import static dev.openfeature.sdk.EvaluationContext.TARGETING_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImmutableContextTest {
    @DisplayName("attributes unable to allow mutation should not affect the immutable context")
    @Test
    void shouldNotAttemptToModifyAttributesForImmutableContext() {
        final Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        // should check the usage of Map.of() which is a more likely use case, but that API isn't available in Java 8
        EvaluationContext ctx = new ImmutableContext("targeting key", Collections.unmodifiableMap(attributes));
        attributes.put("key3", new Value("val3"));
        assertArrayEquals(
                new Object[] {"key1", "key2", TARGETING_KEY}, ctx.keySet().toArray());
    }

    @DisplayName("attributes mutation should not affect the immutable context")
    @Test
    void shouldCreateCopyOfAttributesForImmutableContext() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new ImmutableContext("targeting key", attributes);
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
        EvaluationContext ctx = new ImmutableContext("targeting key", attributes);
        EvaluationContext overriding = new ImmutableContext("overriding_key");
        EvaluationContext merge = ctx.merge(overriding);
        assertEquals("overriding_key", merge.getTargetingKey());
    }

    @DisplayName("targeting key should not be changed from the overriding context if missing")
    @Test
    void shouldRetainTargetingKeyWhenOverridingContextTargetingKeyValueIsEmpty() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new ImmutableContext("targeting_key", attributes);
        EvaluationContext overriding = new ImmutableContext("");
        EvaluationContext merge = ctx.merge(overriding);
        assertEquals("targeting_key", merge.getTargetingKey());
    }

    @DisplayName("missing targeting key should return null")
    @Test
    void missingTargetingKeyShould() {
        EvaluationContext ctx = new ImmutableContext();
        assertNull(ctx.getTargetingKey());
    }

    @DisplayName("Merge should retain all the attributes from the existing context when overriding context is null")
    @Test
    void mergeShouldReturnAllTheValuesFromTheContextWhenOverridingContextIsNull() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new ImmutableContext("targeting_key", attributes);
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

        EvaluationContext ctx = new ImmutableContext("targeting_key", attributes);
        EvaluationContext overriding = new ImmutableContext("targeting_key", overridingAttributes);
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

        EvaluationContext ctx = new ImmutableContext(attributes);
        EvaluationContext overriding = new ImmutableContext();
        EvaluationContext merge = ctx.merge(overriding);
        assertArrayEquals(new Object[] {"key1", "key2"}, merge.keySet().toArray());

        Value key1 = merge.getValue("key1");
        assertTrue(key1.isStructure());

        Structure value = key1.asStructure();
        assertArrayEquals(new Object[] {"key1_1"}, value.keySet().toArray());
    }

    @DisplayName("Merge should obtain keys from the overriding context when the existing context is empty")
    @Test
    void mergeShouldObtainKeysFromOverridingContextWhenExistingContextIsEmpty() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));

        EvaluationContext ctx = new ImmutableContext();
        EvaluationContext overriding = new ImmutableContext(attributes);
        EvaluationContext merge = ctx.merge(overriding);
        assertEquals(new HashSet<>(Arrays.asList("key1", "key2")), merge.keySet());
    }

    @DisplayName("Two ImmutableContext objects with identical attributes are considered equal")
    @Test
    void testImmutableContextEquality() {
        Map<String, Value> map1 = new HashMap<>();
        map1.put("key", new Value("value"));

        Map<String, Value> map2 = new HashMap<>();
        map2.put("key", new Value("value"));

        ImmutableContext a = new ImmutableContext(null, map1);
        ImmutableContext b = new ImmutableContext(null, map2);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @DisplayName("Two different ImmutableContext objects with different contents are not considered equal")
    @Test
    void unequalImmutableContextsAreNotEqual() {
        final Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        final ImmutableContext ctx = new ImmutableContext(attributes);

        final Map<String, Value> attributes2 = new HashMap<>();
        final ImmutableContext ctx2 = new ImmutableContext(attributes2);

        assertNotEquals(ctx, ctx2);
    }

    @DisplayName("ImmutableContext hashCode is stable across multiple invocations")
    @Test
    void immutableContextHashCodeIsStable() {
        Map<String, Value> map = new HashMap<>();
        map.put("key", new Value("value"));

        ImmutableContext ctx = new ImmutableContext(null, map);

        int first = ctx.hashCode();
        int second = ctx.hashCode();
        assertEquals(first, second);
    }
}

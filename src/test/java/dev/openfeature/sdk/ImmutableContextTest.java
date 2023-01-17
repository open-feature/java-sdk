package dev.openfeature.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ImmutableContextTest {

    @Test
    @DisplayName("Mutating targeting key is not allowed on Immutable Context")
    void shouldThrowUnsupportedExceptionWhenMutatingTargetingKey() {
        EvaluationContext ctx = new ImmutableContext("targeting key", new HashMap<>());
        assertThrows(UnsupportedOperationException.class, () -> ctx.setTargetingKey(""));
    }

    @DisplayName("attributes mutation should not affect the immutable context")
    @Test
    void shouldCreateCopyOfAttributesForImmutableContext() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new ImmutableContext("targeting key", attributes);
        attributes.put("key3", new Value("val3"));
        assertArrayEquals(ctx.keySet().toArray(), new Object[]{"key1", "key2"});
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

    @DisplayName("targeting key should not changed from the overriding context if missing")
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

    @DisplayName("Merge should retain all the attributes from the existing context when overriding context is null")
    @Test
    void mergeShouldReturnAllTheValuesFromTheContextWhenOverridingContextIsNull() {
        HashMap<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("val1"));
        attributes.put("key2", new Value("val2"));
        EvaluationContext ctx = new ImmutableContext("targeting_key", attributes);
        EvaluationContext merge = ctx.merge(null);
        assertEquals("targeting_key", merge.getTargetingKey());
        assertArrayEquals(merge.keySet().toArray(), new Object[]{"key1", "key2"});
    }
}

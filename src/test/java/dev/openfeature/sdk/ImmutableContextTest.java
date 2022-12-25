package dev.openfeature.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
}

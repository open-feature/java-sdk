package dev.openfeature.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultEvaluationEventTest {

    @Test
    void builder_shouldCreateEventWithName() {
        String eventName = "test-event";
        EvaluationEvent event = DefaultEvaluationEvent.builder().name(eventName).build();

        assertEquals(eventName, event.getName());
        assertNotNull(event.getAttributes());
        assertTrue(event.getAttributes().isEmpty());
    }

    @Test
    void builder_shouldCreateEventWithAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", 42);

        EvaluationEvent event = DefaultEvaluationEvent.builder()
                .name("test")
                .attributes(attributes)
                .build();

        assertEquals("test", event.getName());
        assertEquals(2, event.getAttributes().size());
        assertEquals("value1", event.getAttributes().get("key1"));
        assertEquals(42, event.getAttributes().get("key2"));
    }

    @Test
    void builder_shouldCreateEventWithIndividualAttribute() {
        EvaluationEvent event = DefaultEvaluationEvent.builder()
                .name("test")
                .attribute("key1", "value1")
                .attribute("key2", 42)
                .build();

        assertEquals("test", event.getName());
        assertEquals(2, event.getAttributes().size());
        assertEquals("value1", event.getAttributes().get("key1"));
        assertEquals(42, event.getAttributes().get("key2"));
    }

    @Test
    void builder_shouldHandleNullAttributes() {
        EvaluationEvent event =
                DefaultEvaluationEvent.builder().name("test").attributes(null).build();

        assertEquals("test", event.getName());
        assertNotNull(event.getAttributes());
        assertTrue(event.getAttributes().isEmpty());
    }

    @Test
    void builder_shouldAllowChaining() {
        EvaluationEvent event = DefaultEvaluationEvent.builder()
                .name("test")
                .attribute("key1", "value1")
                .attribute("key2", "value2")
                .attributes(Map.of("key3", "value3"))
                .attribute("key4", "value4")
                .build();

        assertEquals("test", event.getName());
        assertEquals(2, event.getAttributes().size()); // attributes() overwrites previous attributes
        assertEquals("value3", event.getAttributes().get("key3"));
        assertEquals("value4", event.getAttributes().get("key4"));
    }

    @Test
    void getAttributes_shouldReturnDefensiveCopy() {
        Map<String, Object> original = new HashMap<>();
        original.put("key", "value");

        EvaluationEvent event = DefaultEvaluationEvent.builder()
                .name("test")
                .attributes(original)
                .build();

        Map<String, Object> returned = event.getAttributes();

        // Should not be the same instance
        assertNotSame(original, returned);
        assertNotSame(returned, event.getAttributes()); // Each call returns new instance

        // Modifying returned map should not affect event
        returned.put("newKey", "newValue");
        assertFalse(event.getAttributes().containsKey("newKey"));

        // Modifying original map should not affect event
        original.put("anotherKey", "anotherValue");
        assertFalse(event.getAttributes().containsKey("anotherKey"));
    }

    @Test
    void equals_shouldWorkCorrectly() {
        EvaluationEvent event1 = DefaultEvaluationEvent.builder()
                .name("test")
                .attribute("key", "value")
                .build();

        EvaluationEvent event2 = DefaultEvaluationEvent.builder()
                .name("test")
                .attribute("key", "value")
                .build();

        EvaluationEvent event3 = DefaultEvaluationEvent.builder()
                .name("different")
                .attribute("key", "value")
                .build();

        EvaluationEvent event4 = DefaultEvaluationEvent.builder()
                .name("test")
                .attribute("key", "different")
                .build();

        // Same content should be equal
        assertEquals(event1, event2);
        assertEquals(event2, event1);

        // Different name should not be equal
        assertNotEquals(event1, event3);
        assertNotEquals(event3, event1);

        // Different attributes should not be equal
        assertNotEquals(event1, event4);
        assertNotEquals(event4, event1);

        assertThat(event1)
                // Self-equality
                .isEqualTo(event1)
                // Null comparison
                .isNotEqualTo(null)
                // Different class comparison
                .isNotEqualTo("not an event");
    }

    @Test
    void hashCode_shouldBeConsistent() {
        EvaluationEvent event1 = DefaultEvaluationEvent.builder()
                .name("test")
                .attribute("key", "value")
                .build();

        EvaluationEvent event2 = DefaultEvaluationEvent.builder()
                .name("test")
                .attribute("key", "value")
                .build();

        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void toString_shouldIncludeNameAndAttributes() {
        EvaluationEvent event = DefaultEvaluationEvent.builder()
                .name("test-event")
                .attribute("key", "value")
                .build();

        String toString = event.toString();
        assertTrue(toString.contains("test-event"));
        assertTrue(toString.contains("key"));
        assertTrue(toString.contains("value"));
        assertTrue(toString.contains("EvaluationEvent"));
    }

    @Test
    void builder_shouldHandleEmptyName() {
        EvaluationEvent event = DefaultEvaluationEvent.builder().name("").build();

        assertEquals("", event.getName());
    }

    @Test
    void builder_shouldHandleNullName() {
        EvaluationEvent event = DefaultEvaluationEvent.builder().name(null).build();

        assertNull(event.getName());
    }

    @Test
    void immutability_shouldPreventModificationViaBuilder() {
        DefaultEvaluationEvent.Builder builder =
                DefaultEvaluationEvent.builder().name("test").attribute("key1", "value1");

        EvaluationEvent event = builder.build();

        // Modifying builder after build should not affect built event
        builder.attribute("key2", "value2");

        assertEquals(1, event.getAttributes().size());
        assertFalse(event.getAttributes().containsKey("key2"));
    }
}

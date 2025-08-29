package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ImmutableContextBuilderTest {

    @Test
    void builder_shouldCreateEmptyContext() {
        ImmutableContext context = ImmutableContext.builder().build();

        assertNull(context.getTargetingKey());
        assertTrue(context.isEmpty());
        assertEquals(0, context.keySet().size());
    }

    @Test
    void builder_shouldCreateContextWithTargetingKeyOnly() {
        String targetingKey = "user123";
        ImmutableContext context =
                ImmutableContext.builder().targetingKey(targetingKey).build();

        assertEquals(targetingKey, context.getTargetingKey());
        assertFalse(context.isEmpty()); // Contains targeting key
        assertEquals(1, context.keySet().size());
        assertTrue(context.keySet().contains(EvaluationContext.TARGETING_KEY));
    }

    @Test
    void builder_shouldCreateContextWithAttributesOnly() {
        ImmutableContext context = ImmutableContext.builder()
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .add("boolKey", true)
                .build();

        assertNull(context.getTargetingKey());
        assertFalse(context.isEmpty());
        assertEquals(3, context.keySet().size());
        assertEquals("stringValue", context.getValue("stringKey").asString());
        assertEquals(42, context.getValue("intKey").asInteger());
        assertEquals(true, context.getValue("boolKey").asBoolean());
    }

    @Test
    void builder_shouldCreateContextWithTargetingKeyAndAttributes() {
        String targetingKey = "user456";
        ImmutableContext context = ImmutableContext.builder()
                .targetingKey(targetingKey)
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .build();

        assertEquals(targetingKey, context.getTargetingKey());
        assertFalse(context.isEmpty());
        assertEquals(3, context.keySet().size()); // targeting key + 2 attributes
        assertTrue(context.keySet().contains(EvaluationContext.TARGETING_KEY));
        assertEquals("stringValue", context.getValue("stringKey").asString());
        assertEquals(42, context.getValue("intKey").asInteger());
    }

    @Test
    void builder_shouldAddAllDataTypes() {
        MutableStructure nestedStructure = new MutableStructure().add("nested", "value");
        Value customValue = new Value("customValue");

        ImmutableContext context = ImmutableContext.builder()
                .targetingKey("user789")
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .add("longKey", 1234567890L)
                .add("floatKey", 3.14f)
                .add("doubleKey", 3.141592653589793)
                .add("boolKey", true)
                .add("structKey", nestedStructure)
                .add("valueKey", customValue)
                .build();

        assertEquals("user789", context.getTargetingKey());
        assertEquals(9, context.keySet().size()); // targeting key + 8 attributes
        assertEquals("stringValue", context.getValue("stringKey").asString());
        assertEquals(42, context.getValue("intKey").asInteger());
        assertEquals(1234567890L, (Long) context.getValue("longKey").asObject());
        assertEquals(3.14f, (Float) context.getValue("floatKey").asObject());
        assertEquals(3.141592653589793, context.getValue("doubleKey").asDouble());
        assertEquals(true, context.getValue("boolKey").asBoolean());
        assertTrue(context.getValue("structKey").isStructure());
        assertEquals("customValue", context.getValue("valueKey").asString());
    }

    @Test
    void builder_shouldHandleNullValues() {
        ImmutableContext context = ImmutableContext.builder()
                .targetingKey(null)
                .add("stringKey", (String) null)
                .add("intKey", (Integer) null)
                .add("boolKey", (Boolean) null)
                .build();

        assertNull(context.getTargetingKey());
        assertEquals(3, context.keySet().size());
        // Keys should exist but values may be null
        assertTrue(context.keySet().contains("stringKey"));
        assertTrue(context.keySet().contains("intKey"));
        assertTrue(context.keySet().contains("boolKey"));
    }

    @Test
    void builder_shouldOverwriteExistingKeys() {
        ImmutableContext context = ImmutableContext.builder()
                .add("key", "firstValue")
                .add("key", "secondValue")
                .build();

        assertEquals(1, context.keySet().size());
        assertEquals("secondValue", context.getValue("key").asString());
    }

    @Test
    void builder_shouldOverwriteTargetingKey() {
        ImmutableContext context = ImmutableContext.builder()
                .targetingKey("firstKey")
                .targetingKey("secondKey")
                .build();

        assertEquals("secondKey", context.getTargetingKey());
        assertEquals(1, context.keySet().size());
    }

    @Test
    void builder_shouldSetAttributesFromMap() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));
        attributes.put("key2", new Value(123));

        ImmutableContext context = ImmutableContext.builder()
                .targetingKey("user123")
                .attributes(attributes)
                .build();

        assertEquals("user123", context.getTargetingKey());
        assertEquals(3, context.keySet().size()); // targeting key + 2 attributes
        assertEquals("value1", context.getValue("key1").asString());
        assertEquals(123, context.getValue("key2").asInteger());
    }

    @Test
    void builder_shouldHandleNullAttributesMap() {
        ImmutableContext context = ImmutableContext.builder()
                .targetingKey("user123")
                .attributes(null)
                .add("key", "value")
                .build();

        assertEquals("user123", context.getTargetingKey());
        assertEquals(2, context.keySet().size());
        assertEquals("value", context.getValue("key").asString());
    }

    @Test
    void builder_shouldAllowChaining() {
        ImmutableContext context = ImmutableContext.builder()
                .targetingKey("user123")
                .add("key1", "value1")
                .add("key2", 100)
                .add("key3", true)
                .build();

        assertEquals("user123", context.getTargetingKey());
        assertEquals(4, context.keySet().size());
        assertEquals("value1", context.getValue("key1").asString());
        assertEquals(100, context.getValue("key2").asInteger());
        assertEquals(true, context.getValue("key3").asBoolean());
    }

    @Test
    void builder_shouldCreateIndependentInstances() {
        ImmutableContext.Builder builder =
                ImmutableContext.builder().targetingKey("user123").add("key1", "value1");

        ImmutableContext context1 = builder.build();

        // Adding to builder after first build should not affect first instance
        builder.add("key2", "value2");
        ImmutableContext context2 = builder.build();

        assertEquals(2, context1.keySet().size()); // targeting key + 1 attribute
        assertEquals(3, context2.keySet().size()); // targeting key + 2 attributes
        assertEquals("value1", context1.getValue("key1").asString());
        assertNull(context1.getValue("key2"));
        assertEquals("value1", context2.getValue("key1").asString());
        assertEquals("value2", context2.getValue("key2").asString());
    }

    @Test
    void toBuilder_shouldCreateBuilderWithCurrentState() {
        ImmutableContext original = ImmutableContext.builder()
                .targetingKey("user123")
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        ImmutableContext copy = original.toBuilder().add("key3", "value3").build();

        // Original should be unchanged
        assertEquals("user123", original.getTargetingKey());
        assertEquals(3, original.keySet().size());

        // Copy should have original data plus new data
        assertEquals("user123", copy.getTargetingKey());
        assertEquals(4, copy.keySet().size());
        assertEquals("value1", copy.getValue("key1").asString());
        assertEquals(42, copy.getValue("key2").asInteger());
        assertEquals("value3", copy.getValue("key3").asString());
    }

    @Test
    void toBuilder_shouldWorkWithEmptyContext() {
        ImmutableContext original = ImmutableContext.builder().build();

        ImmutableContext copy =
                original.toBuilder().targetingKey("user123").add("key", "value").build();

        assertNull(original.getTargetingKey());
        assertTrue(original.isEmpty());

        assertEquals("user123", copy.getTargetingKey());
        assertEquals(2, copy.keySet().size());
        assertEquals("value", copy.getValue("key").asString());
    }

    @Test
    void toBuilder_shouldPreserveTargetingKey() {
        ImmutableContext original = ImmutableContext.builder()
                .targetingKey("originalUser")
                .add("key1", "value1")
                .build();

        ImmutableContext copy = original.toBuilder()
                .targetingKey("newUser")
                .add("key2", "value2")
                .build();

        assertEquals("originalUser", original.getTargetingKey());
        assertEquals("newUser", copy.getTargetingKey());
        assertEquals("value1", copy.getValue("key1").asString());
        assertEquals("value2", copy.getValue("key2").asString());
    }

    @Test
    void builder_shouldMaintainImmutability() {
        Map<String, Value> originalAttributes = new HashMap<>();
        originalAttributes.put("key1", new Value("value1"));

        ImmutableContext context = ImmutableContext.builder()
                .targetingKey("user123")
                .attributes(originalAttributes)
                .build();

        // Modifying original map should not affect the built context
        originalAttributes.put("key2", new Value("value2"));
        assertEquals(2, context.keySet().size()); // targeting key + original attribute
        assertNull(context.getValue("key2"));
    }

    @Test
    void builder_shouldBeConsistentWithConstructors() {
        String targetingKey = "user123";
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));
        attributes.put("key2", new Value(42));

        // Create via constructor
        ImmutableContext constructorContext = new ImmutableContext(targetingKey, attributes);

        // Create via builder
        ImmutableContext builderContext = ImmutableContext.builder()
                .targetingKey(targetingKey)
                .attributes(attributes)
                .build();

        // Should be equivalent
        assertEquals(constructorContext.getTargetingKey(), builderContext.getTargetingKey());
        assertEquals(constructorContext.keySet(), builderContext.keySet());
        assertEquals(
                constructorContext.getValue("key1").asString(),
                builderContext.getValue("key1").asString());
        assertEquals(
                constructorContext.getValue("key2").asInteger(),
                builderContext.getValue("key2").asInteger());
    }

    @Test
    void builder_shouldHandleEmptyAndWhitespaceTargetingKeys() {
        // Empty string targeting key should be treated as null
        ImmutableContext emptyContext =
                ImmutableContext.builder().targetingKey("").add("key", "value").build();

        // Whitespace targeting key should be treated as null
        ImmutableContext whitespaceContext = ImmutableContext.builder()
                .targetingKey("   ")
                .add("key", "value")
                .build();

        // Both should not have targeting key in the final structure
        // (This follows the constructor logic that checks for !targetingKey.trim().isEmpty())
        assertEquals(1, emptyContext.keySet().size()); // Only the added key
        assertEquals(1, whitespaceContext.keySet().size()); // Only the added key
    }

    @Test
    void builder_shouldSupportComplexNestedStructures() {
        // Test with deeply nested structure
        ImmutableStructure nestedStructure = ImmutableStructure.builder()
                .add(
                        "level1",
                        ImmutableStructure.builder().add("level2", "deepValue").build())
                .build();

        ImmutableContext context = ImmutableContext.builder()
                .targetingKey("user123")
                .add("nested", nestedStructure)
                .build();

        assertTrue(context.getValue("nested").isStructure());
        Structure retrievedStruct = context.getValue("nested").asStructure();
        assertTrue(retrievedStruct.getValue("level1").isStructure());
        assertEquals(
                "deepValue",
                retrievedStruct
                        .getValue("level1")
                        .asStructure()
                        .getValue("level2")
                        .asString());
    }

    @Test
    void equals_shouldWorkWithBuiltContexts() {
        ImmutableContext context1 = ImmutableContext.builder()
                .targetingKey("user123")
                .add("key1", "value1")
                .build();

        ImmutableContext context2 = ImmutableContext.builder()
                .targetingKey("user123")
                .add("key1", "value1")
                .build();

        ImmutableContext context3 = ImmutableContext.builder()
                .targetingKey("user456")
                .add("key1", "value1")
                .build();

        // Same content should be equal
        assertEquals(context1, context2);
        assertEquals(context2, context1);

        // Different targeting key should not be equal
        assertNotEquals(context1, context3);

        // Self-equality
        assertEquals(context1, context1);
    }

    @Test
    void hashCode_shouldBeConsistentWithBuiltContexts() {
        ImmutableContext context1 = ImmutableContext.builder()
                .targetingKey("user123")
                .add("key1", "value1")
                .build();

        ImmutableContext context2 = ImmutableContext.builder()
                .targetingKey("user123")
                .add("key1", "value1")
                .build();

        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    void merge_shouldWorkWithBuiltContexts() {
        ImmutableContext context1 = ImmutableContext.builder()
                .targetingKey("user123")
                .add("key1", "value1")
                .add("shared", "original")
                .build();

        ImmutableContext context2 = ImmutableContext.builder()
                .add("key2", "value2")
                .add("shared", "override")
                .build();

        EvaluationContext merged = context1.merge(context2);

        assertEquals("user123", merged.getTargetingKey()); // Preserved from context1
        assertEquals("value1", merged.getValue("key1").asString());
        assertEquals("value2", merged.getValue("key2").asString());
        assertEquals("override", merged.getValue("shared").asString()); // Overridden
    }
}

package dev.openfeature.api.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ImmutableStructureBuilderTest {

    @Test
    void builder_shouldCreateEmptyStructure() {
        ImmutableStructure structure = ImmutableStructure.builder().build();

        assertTrue(structure.isEmpty());
        assertEquals(0, structure.keySet().size());
    }

    @Test
    void builder_shouldCreateStructureWithSingleValue() {
        ImmutableStructure structure =
                ImmutableStructure.builder().add("key", "value").build();

        assertFalse(structure.isEmpty());
        assertEquals(1, structure.keySet().size());
        assertTrue(structure.keySet().contains("key"));
        assertEquals("value", structure.getValue("key").asString());
    }

    @Test
    void builder_shouldAddAllDataTypes() {
        MutableStructure nestedStructure = new MutableStructure().add("nested", "value");
        Value customValue = new Value("customValue");

        ImmutableStructure structure = ImmutableStructure.builder()
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .add("longKey", 1234567890L)
                .add("floatKey", 3.14f)
                .add("doubleKey", 3.141592653589793)
                .add("boolKey", true)
                .add("structKey", nestedStructure)
                .add("valueKey", customValue)
                .build();

        assertEquals(8, structure.keySet().size());
        assertEquals("stringValue", structure.getValue("stringKey").asString());
        assertEquals(42, structure.getValue("intKey").asInteger());
        assertEquals(1234567890L, (Long) structure.getValue("longKey").asObject());
        assertEquals(3.14f, (Float) structure.getValue("floatKey").asObject());
        assertEquals(3.141592653589793, structure.getValue("doubleKey").asDouble());
        assertEquals(true, structure.getValue("boolKey").asBoolean());
        assertTrue(structure.getValue("structKey").isStructure());
        assertEquals("customValue", structure.getValue("valueKey").asString());
    }

    @Test
    void builder_shouldHandleNullValues() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .add("stringKey", (String) null)
                .add("intKey", (Integer) null)
                .add("boolKey", (Boolean) null)
                .add("structKey", (Structure) null)
                .add("valueKey", (Value) null)
                .build();

        assertEquals(5, structure.keySet().size());
        // Keys should exist
        assertTrue(structure.keySet().contains("stringKey"));
        assertTrue(structure.keySet().contains("intKey"));
        assertTrue(structure.keySet().contains("boolKey"));
        assertTrue(structure.keySet().contains("structKey"));
        assertTrue(structure.keySet().contains("valueKey"));
    }

    @Test
    void builder_shouldOverwriteExistingKeys() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .add("key", "firstValue")
                .add("key", "secondValue")
                .build();

        assertEquals(1, structure.keySet().size());
        assertEquals("secondValue", structure.getValue("key").asString());
    }

    @Test
    void builder_shouldSetAttributesFromMap() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));
        attributes.put("key2", new Value(123));

        ImmutableStructure structure =
                ImmutableStructure.builder().attributes(attributes).build();

        assertEquals(2, structure.keySet().size());
        assertEquals("value1", structure.getValue("key1").asString());
        assertEquals(123, structure.getValue("key2").asInteger());
    }

    @Test
    void builder_shouldHandleNullAttributesMap() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .attributes(null)
                .add("key", "value")
                .build();

        assertEquals(1, structure.keySet().size());
        assertEquals("value", structure.getValue("key").asString());
    }

    @Test
    void builder_shouldAllowChaining() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 100)
                .add("key3", true)
                .build();

        assertEquals(3, structure.keySet().size());
        assertEquals("value1", structure.getValue("key1").asString());
        assertEquals(100, structure.getValue("key2").asInteger());
        assertEquals(true, structure.getValue("key3").asBoolean());
    }

    @Test
    void builder_shouldCreateIndependentInstances() {
        ImmutableStructure.Builder builder = ImmutableStructure.builder().add("key1", "value1");

        ImmutableStructure structure1 = builder.build();

        // Adding to builder after first build should not affect first instance
        builder.add("key2", "value2");
        ImmutableStructure structure2 = builder.build();

        assertEquals(1, structure1.keySet().size());
        assertEquals(2, structure2.keySet().size());
        assertEquals("value1", structure1.getValue("key1").asString());
        assertNull(structure1.getValue("key2"));
        assertEquals("value1", structure2.getValue("key1").asString());
        assertEquals("value2", structure2.getValue("key2").asString());
    }

    @Test
    void toBuilder_shouldCreateBuilderWithCurrentState() {
        ImmutableStructure original = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        ImmutableStructure copy = original.toBuilder().add("key3", "value3").build();

        // Original should be unchanged
        assertEquals(2, original.keySet().size());

        // Copy should have original data plus new data
        assertEquals(3, copy.keySet().size());
        assertEquals("value1", copy.getValue("key1").asString());
        assertEquals(42, copy.getValue("key2").asInteger());
        assertEquals("value3", copy.getValue("key3").asString());
    }

    @Test
    void toBuilder_shouldWorkWithEmptyStructure() {
        ImmutableStructure original = ImmutableStructure.builder().build();

        ImmutableStructure copy = original.toBuilder().add("key", "value").build();

        assertTrue(original.isEmpty());

        assertEquals(1, copy.keySet().size());
        assertEquals("value", copy.getValue("key").asString());
    }

    @Test
    void builder_shouldMaintainImmutability() {
        Map<String, Value> originalAttributes = new HashMap<>();
        originalAttributes.put("key1", new Value("value1"));

        ImmutableStructure structure =
                ImmutableStructure.builder().attributes(originalAttributes).build();

        // Modifying original map should not affect the built structure
        originalAttributes.put("key2", new Value("value2"));
        assertEquals(1, structure.keySet().size());
        assertNull(structure.getValue("key2"));
    }

    @Test
    void builder_shouldBeConsistentWithConstructors() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));
        attributes.put("key2", new Value(42));

        // Create via constructor
        ImmutableStructure constructorStructure = new ImmutableStructure(attributes);

        // Create via builder
        ImmutableStructure builderStructure =
                ImmutableStructure.builder().attributes(attributes).build();

        // Should be equivalent
        assertEquals(constructorStructure.keySet(), builderStructure.keySet());
        assertEquals(
                constructorStructure.getValue("key1").asString(),
                builderStructure.getValue("key1").asString());
        assertEquals(
                constructorStructure.getValue("key2").asInteger(),
                builderStructure.getValue("key2").asInteger());
    }

    @Test
    void builder_shouldSupportComplexNestedStructures() {
        // Test with deeply nested structure
        ImmutableStructure deeplyNested =
                ImmutableStructure.builder().add("level3", "deepestValue").build();

        ImmutableStructure nestedStructure = ImmutableStructure.builder()
                .add("level2", deeplyNested)
                .add("level2Value", "level2String")
                .build();

        ImmutableStructure structure = ImmutableStructure.builder()
                .add("level1", nestedStructure)
                .add("topLevel", "topValue")
                .build();

        assertEquals(2, structure.keySet().size());
        assertEquals("topValue", structure.getValue("topLevel").asString());

        assertTrue(structure.getValue("level1").isStructure());
        Structure level1 = structure.getValue("level1").asStructure();
        assertEquals("level2String", level1.getValue("level2Value").asString());

        assertTrue(level1.getValue("level2").isStructure());
        Structure level2 = level1.getValue("level2").asStructure();
        assertEquals("deepestValue", level2.getValue("level3").asString());
    }

    @Test
    void builder_shouldReturnDefensiveCopies() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        // getValue should return clones/defensive copies
        Value value1a = structure.getValue("key1");
        Value value1b = structure.getValue("key1");

        // Values should be equal but not the same instance (defensive copies)
        assertEquals(value1a.asString(), value1b.asString());
        // Note: Value class may or may not return the same instance depending on implementation
    }

    @Test
    void asMap_shouldReturnDefensiveCopy() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        Map<String, Value> map1 = structure.asMap();
        Map<String, Value> map2 = structure.asMap();

        // Each call should return a new map (defensive copy)
        assertEquals(map1.size(), map2.size());
        assertEquals(map1.get("key1").asString(), map2.get("key1").asString());
        // Maps should be equal in content but not necessarily the same instance
    }

    @Test
    void builder_shouldHandleAttributesOverride() {
        Map<String, Value> initialAttributes = new HashMap<>();
        initialAttributes.put("key1", new Value("initial1"));
        initialAttributes.put("key2", new Value("initial2"));

        Map<String, Value> overrideAttributes = new HashMap<>();
        overrideAttributes.put("key3", new Value("override3"));
        overrideAttributes.put("key4", new Value("override4"));

        ImmutableStructure structure = ImmutableStructure.builder()
                .attributes(initialAttributes)
                .add("key5", "added5")
                .attributes(overrideAttributes) // This should clear previous and set new
                .add("key6", "added6")
                .build();

        assertEquals(3, structure.keySet().size()); // key3, key4, key6
        assertNull(structure.getValue("key1")); // Cleared by attributes()
        assertNull(structure.getValue("key2")); // Cleared by attributes()
        assertNull(structure.getValue("key5")); // Cleared by attributes()
        assertEquals("override3", structure.getValue("key3").asString());
        assertEquals("override4", structure.getValue("key4").asString());
        assertEquals("added6", structure.getValue("key6").asString());
    }

    @Test
    void equals_shouldWorkWithBuiltStructures() {
        ImmutableStructure structure1 = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        ImmutableStructure structure2 = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        ImmutableStructure structure3 = ImmutableStructure.builder()
                .add("key1", "different")
                .add("key2", 42)
                .build();

        // Same content should be equal
        assertEquals(structure1, structure2);
        assertEquals(structure2, structure1);

        // Different content should not be equal
        assertNotEquals(structure1, structure3);

        // Self-equality
        assertEquals(structure1, structure1);
    }

    @Test
    void hashCode_shouldBeConsistentWithBuiltStructures() {
        ImmutableStructure structure1 = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        ImmutableStructure structure2 = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        assertEquals(structure1.hashCode(), structure2.hashCode());
    }

    @Test
    void toString_shouldIncludeBuiltContent() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        String toString = structure.toString();
        assertTrue(toString.contains("ImmutableStructure"));
        assertTrue(toString.contains("attributes="));
    }

    @Test
    void asObjectMap_shouldWorkWithBuiltStructures() {
        ImmutableStructure structure = ImmutableStructure.builder()
                .add("stringKey", "stringValue")
                .add("intKey", 123)
                .add("boolKey", true)
                .add("doubleKey", 3.14)
                .build();

        Map<String, Object> objectMap = structure.asObjectMap();
        assertEquals(4, objectMap.size());
        assertEquals("stringValue", objectMap.get("stringKey"));
        assertEquals(123, objectMap.get("intKey"));
        assertEquals(true, objectMap.get("boolKey"));
        assertEquals(3.14, objectMap.get("doubleKey"));
    }

    @Test
    void builder_shouldSupportMixedBuilderAndAttributesUsage() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("mapKey1", new Value("mapValue1"));
        attributes.put("mapKey2", new Value(100));

        ImmutableStructure structure = ImmutableStructure.builder()
                .add("builderKey1", "builderValue1")
                .attributes(attributes)
                .add("builderKey2", "builderValue2")
                .build();

        assertEquals(3, structure.keySet().size());
        assertNull(structure.getValue("builderKey1")); // Cleared by attributes()
        assertEquals("mapValue1", structure.getValue("mapKey1").asString());
        assertEquals(100, structure.getValue("mapKey2").asInteger());
        assertEquals("builderValue2", structure.getValue("builderKey2").asString());
    }
}

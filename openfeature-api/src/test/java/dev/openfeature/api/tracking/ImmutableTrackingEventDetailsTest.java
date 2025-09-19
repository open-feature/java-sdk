package dev.openfeature.api.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.types.ImmutableStructure;
import dev.openfeature.api.types.Value;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ImmutableTrackingEventDetailsTest {

    @Test
    void builder_shouldCreateEmptyDetailsWithoutValue() {
        TrackingEventDetails details = TrackingEventDetails.EMPTY;

        assertEquals(Optional.empty(), details.getValue());
        assertTrue(details.isEmpty());
        assertEquals(0, details.keySet().size());
    }

    @Test
    void builder_shouldCreateDetailsWithValue() {
        Number value = 42;
        TrackingEventDetails details =
                TrackingEventDetails.immutableBuilder().value(value).build();

        assertEquals(Optional.of(value), details.getValue());
        assertTrue(details.isEmpty()); // Structure is empty
    }

    @Test
    void builder_shouldCreateDetailsWithValueAndAttributes() {
        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(3.14)
                .add("key1", "value1")
                .add("key2", 123)
                .build();

        assertEquals(Optional.of(3.14), details.getValue());
        assertFalse(details.isEmpty());
        assertEquals(2, details.keySet().size());
        assertEquals("value1", details.getValue("key1").asString());
        assertEquals(123, details.getValue("key2").asInteger());
    }

    @Test
    void constructor_shouldCreateEmptyDetailsWithoutValue() {
        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails();

        assertEquals(Optional.empty(), details.getValue());
        assertTrue(details.isEmpty());
        assertEquals(0, details.keySet().size());
    }

    @Test
    void constructor_shouldCreateDetailsWithValue() {
        Number value = 42;
        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(value);

        assertEquals(Optional.of(value), details.getValue());
        assertTrue(details.isEmpty()); // Structure is empty
    }

    @Test
    void constructor_shouldCreateDetailsWithValueAndAttributes() {
        Number value = 3.14;
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));
        attributes.put("key2", new Value(123));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(value, attributes);

        assertEquals(Optional.of(value), details.getValue());
        assertFalse(details.isEmpty());
        assertEquals(2, details.keySet().size());
        assertEquals("value1", details.getValue("key1").asString());
        assertEquals(123, details.getValue("key2").asInteger());
    }

    @Test
    void constructor_shouldHandleNullValue() {
        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(null);

        assertEquals(Optional.empty(), details.getValue());
        assertTrue(details.isEmpty());
    }

    @Test
    void constructor_shouldHandleNullAttributes() {
        Number value = 42;
        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(value, null);

        assertEquals(Optional.of(value), details.getValue());
        assertTrue(details.isEmpty());
    }

    @Test
    void getValue_shouldReturnCorrectValueTypes() {
        // Test with Integer
        ImmutableTrackingEventDetails intDetails = new ImmutableTrackingEventDetails(42);
        assertEquals(Optional.of(42), intDetails.getValue());
        assertEquals(Integer.class, intDetails.getValue().get().getClass());

        // Test with Double
        ImmutableTrackingEventDetails doubleDetails = new ImmutableTrackingEventDetails(3.14);
        assertEquals(Optional.of(3.14), doubleDetails.getValue());
        assertEquals(Double.class, doubleDetails.getValue().get().getClass());

        // Test with Long
        ImmutableTrackingEventDetails longDetails = new ImmutableTrackingEventDetails(123456789L);
        assertEquals(Optional.of(123456789L), longDetails.getValue());
        assertEquals(Long.class, longDetails.getValue().get().getClass());

        // Test with Float
        ImmutableTrackingEventDetails floatDetails = new ImmutableTrackingEventDetails(2.71f);
        assertEquals(Optional.of(2.71f), floatDetails.getValue());
        assertEquals(Float.class, floatDetails.getValue().get().getClass());
    }

    @Test
    void structureDelegation_shouldWorkCorrectly() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("stringKey", new Value("stringValue"));
        attributes.put("boolKey", new Value(true));
        attributes.put("intKey", new Value(456));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(100, attributes);

        // Test delegation to structure methods
        assertFalse(details.isEmpty());
        assertEquals(3, details.keySet().size());
        assertTrue(details.keySet().contains("stringKey"));
        assertTrue(details.keySet().contains("boolKey"));
        assertTrue(details.keySet().contains("intKey"));

        // Test getValue delegation
        assertEquals("stringValue", details.getValue("stringKey").asString());
        assertEquals(true, details.getValue("boolKey").asBoolean());
        assertEquals(456, details.getValue("intKey").asInteger());
    }

    @Test
    void asMap_shouldReturnStructureMap() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(42, attributes);

        Map<String, Value> resultMap = details.asMap();
        assertEquals(1, resultMap.size());
        assertEquals("value1", resultMap.get("key1").asString());
    }

    @Test
    void asUnmodifiableMap_shouldReturnUnmodifiableMap() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(42, attributes);

        Map<String, Value> unmodifiableMap = details.asUnmodifiableMap();
        assertEquals(1, unmodifiableMap.size());
        assertEquals("value1", unmodifiableMap.get("key1").asString());

        // The unmodifiability is enforced by the underlying ImmutableStructure
    }

    @Test
    void asObjectMap_shouldReturnObjectMap() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("stringKey", new Value("stringValue"));
        attributes.put("intKey", new Value(123));
        attributes.put("boolKey", new Value(true));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(42, attributes);

        Map<String, Object> objectMap = details.asObjectMap();
        assertEquals(3, objectMap.size());
        assertEquals("stringValue", objectMap.get("stringKey"));
        assertEquals(123, objectMap.get("intKey"));
        assertEquals(true, objectMap.get("boolKey"));
    }

    @Test
    void equals_shouldWorkCorrectly() {
        Map<String, Value> attributes1 = new HashMap<>();
        attributes1.put("key1", new Value("value1"));

        Map<String, Value> attributes2 = new HashMap<>();
        attributes2.put("key1", new Value("value1"));

        Map<String, Value> attributes3 = new HashMap<>();
        attributes3.put("key1", new Value("different"));

        ImmutableTrackingEventDetails details1 = new ImmutableTrackingEventDetails(42, attributes1);
        ImmutableTrackingEventDetails details2 = new ImmutableTrackingEventDetails(42, attributes2);
        ImmutableTrackingEventDetails details3 = new ImmutableTrackingEventDetails(42, attributes3);
        ImmutableTrackingEventDetails details4 = new ImmutableTrackingEventDetails(99, attributes1);
        ImmutableTrackingEventDetails details5 = new ImmutableTrackingEventDetails();

        // Same content should be equal
        assertEquals(details1, details2);
        assertEquals(details2, details1);

        // Different structure should not be equal
        assertNotEquals(details1, details3);

        // Different value should not be equal
        assertNotEquals(details1, details4);

        // Self-equality
        assertEquals(details1, details1);

        // Null comparison
        assertNotEquals(details1, null);

        // Different class comparison
        assertNotEquals(details1, "not a details object");

        // Empty details
        ImmutableTrackingEventDetails emptyDetails = new ImmutableTrackingEventDetails();
        assertEquals(details5, emptyDetails);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        Map<String, Value> attributes1 = new HashMap<>();
        attributes1.put("key1", new Value("value1"));

        Map<String, Value> attributes2 = new HashMap<>();
        attributes2.put("key1", new Value("value1"));

        ImmutableTrackingEventDetails details1 = new ImmutableTrackingEventDetails(42, attributes1);
        ImmutableTrackingEventDetails details2 = new ImmutableTrackingEventDetails(42, attributes2);

        assertEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void toString_shouldIncludeValueAndStructure() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(42, attributes);

        String toString = details.toString();
        assertTrue(toString.contains("ImmutableTrackingEventDetails"));
        assertTrue(toString.contains("value=42"));
        assertTrue(toString.contains("structure="));
    }

    @Test
    void toString_shouldHandleNullValue() {
        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails();

        String toString = details.toString();
        assertTrue(toString.contains("ImmutableTrackingEventDetails"));
        assertTrue(toString.contains("value=null"));
    }

    @Test
    void immutability_shouldPreventStructureModification() {
        Map<String, Value> originalAttributes = new HashMap<>();
        originalAttributes.put("key1", new Value("value1"));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(42, originalAttributes);

        // Modifying original map should not affect the details
        originalAttributes.put("key2", new Value("value2"));
        assertEquals(1, details.keySet().size());
        assertFalse(details.keySet().contains("key2"));
    }

    @Test
    void differentValueTypes_shouldNotBeEqual() {
        ImmutableTrackingEventDetails intDetails = new ImmutableTrackingEventDetails(42);
        ImmutableTrackingEventDetails doubleDetails = new ImmutableTrackingEventDetails(42.0);

        // Even though numeric values are "equal", they should not be equal as objects
        assertNotEquals(intDetails, doubleDetails);
    }

    @Test
    void structureInterface_shouldSupportComplexStructures() {
        // Test with nested structure
        Map<String, Value> nestedAttributes = new HashMap<>();
        nestedAttributes.put("nested", new Value("nestedValue"));
        ImmutableStructure nestedStructure = new ImmutableStructure(nestedAttributes);

        Map<String, Value> attributes = new HashMap<>();
        attributes.put("nested_structure", new Value(nestedStructure));

        ImmutableTrackingEventDetails details = new ImmutableTrackingEventDetails(42, attributes);

        assertFalse(details.isEmpty());
        assertTrue(details.getValue("nested_structure").isStructure());
        assertEquals(
                "nestedValue",
                details.getValue("nested_structure")
                        .asStructure()
                        .getValue("nested")
                        .asString());
    }

    // Builder-specific tests
    @Test
    void builder_shouldAddAllNumericTypes() {
        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(100)
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .add("longKey", 1234567890L)
                .add("floatKey", 3.14f)
                .add("doubleKey", 3.141592653589793)
                .add("boolKey", true)
                .build();

        assertEquals(Optional.of(100), details.getValue());
        assertEquals(6, details.keySet().size());
        assertEquals("stringValue", details.getValue("stringKey").asString());
        assertEquals(Integer.valueOf(42), details.getValue("intKey").asInteger());
        assertEquals(
                Long.valueOf(1234567890L), (Long) details.getValue("longKey").asObject());
        assertEquals(Float.valueOf(3.14f), (Float) details.getValue("floatKey").asObject());
        assertEquals(
                Double.valueOf(3.141592653589793), details.getValue("doubleKey").asDouble());
        assertEquals(Boolean.TRUE, details.getValue("boolKey").asBoolean());
    }

    @Test
    void builder_shouldHandleNullValues() {
        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(null)
                .add("stringKey", (String) null)
                .add("intKey", (Integer) null)
                .add("longKey", (Long) null)
                .add("floatKey", (Float) null)
                .add("doubleKey", (Double) null)
                .add("boolKey", (Boolean) null)
                .build();

        assertEquals(Optional.empty(), details.getValue());
        assertEquals(6, details.keySet().size());
        // The null values will be stored as Value objects containing null
    }

    @Test
    void builder_shouldSupportStructureAndValue() {
        Map<String, Value> nestedAttributes = new HashMap<>();
        nestedAttributes.put("nested", new Value("nestedValue"));
        ImmutableStructure nestedStructure = new ImmutableStructure(nestedAttributes);
        Value customValue = new Value("customValue");

        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(42)
                .add("structKey", nestedStructure)
                .add("valueKey", customValue)
                .build();

        assertEquals(Optional.of(42), details.getValue());
        assertEquals(2, details.keySet().size());
        assertTrue(details.getValue("structKey").isStructure());
        assertEquals(
                "nestedValue",
                details.getValue("structKey").asStructure().getValue("nested").asString());
        assertEquals("customValue", details.getValue("valueKey").asString());
    }

    @Test
    void builder_shouldAllowChaining() {
        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(42)
                .add("key1", "value1")
                .add("key2", 100)
                .add("key3", true)
                .build();

        assertEquals(Optional.of(42), details.getValue());
        assertEquals(3, details.keySet().size());
        assertEquals("value1", details.getValue("key1").asString());
        assertEquals(Integer.valueOf(100), details.getValue("key2").asInteger());
        assertEquals(Boolean.TRUE, details.getValue("key3").asBoolean());
    }

    @Test
    void builder_shouldOverwriteExistingKeys() {
        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .add("key", "firstValue")
                .add("key", "secondValue")
                .build();

        assertEquals(1, details.keySet().size());
        assertEquals("secondValue", details.getValue("key").asString());
    }

    @Test
    void builder_shouldSetAttributesFromMap() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("key1", new Value("value1"));
        attributes.put("key2", new Value(123));

        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(42)
                .attributes(attributes)
                .build();

        assertEquals(Optional.of(42), details.getValue());
        assertEquals(2, details.keySet().size());
        assertEquals("value1", details.getValue("key1").asString());
        assertEquals(123, details.getValue("key2").asInteger());
    }

    @Test
    void builder_shouldHandleNullAttributesMap() {
        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(42)
                .attributes(null)
                .add("key", "value")
                .build();

        assertEquals(Optional.of(42), details.getValue());
        assertEquals(1, details.keySet().size());
        assertEquals("value", details.getValue("key").asString());
    }

    @Test
    void builder_shouldCreateIndependentInstances() {

        ImmutableTrackingEventDetailsBuilder builder =
                TrackingEventDetails.immutableBuilder().value(42).add("key1", "value1");

        TrackingEventDetails details1 = builder.build();

        // Adding to builder after first build should not affect first instance
        builder.add("key2", "value2");
        TrackingEventDetails details2 = builder.build();

        assertEquals(1, details1.keySet().size());
        assertEquals(2, details2.keySet().size());
        assertEquals("value1", details1.getValue("key1").asString());
        assertEquals("value1", details2.getValue("key1").asString());
        assertEquals("value2", details2.getValue("key2").asString());
    }

    @Test
    void builder_shouldMaintainImmutability() {
        Map<String, Value> originalAttributes = new HashMap<>();
        originalAttributes.put("key1", new Value("value1"));

        TrackingEventDetails details = TrackingEventDetails.immutableBuilder()
                .value(42)
                .attributes(originalAttributes)
                .build();

        // Modifying original map should not affect the built details
        originalAttributes.put("key2", new Value("value2"));
        assertEquals(1, details.keySet().size());
        assertFalse(details.keySet().contains("key2"));
    }
}

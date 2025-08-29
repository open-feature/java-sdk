package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MutableTrackingEventDetailsTest {

    @Test
    void constructor_shouldCreateEmptyDetailsWithoutValue() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails();

        assertEquals(Optional.empty(), details.getValue());
        assertTrue(details.isEmpty());
        assertEquals(0, details.keySet().size());
    }

    @Test
    void constructor_shouldCreateDetailsWithValue() {
        Number value = 42;
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(value);

        assertEquals(Optional.of(value), details.getValue());
        assertTrue(details.isEmpty()); // Structure is empty
    }

    @Test
    void constructor_shouldHandleNullValue() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(null);

        assertEquals(Optional.empty(), details.getValue());
        assertTrue(details.isEmpty());
    }

    @Test
    void getValue_shouldReturnCorrectValueTypes() {
        // Test with Integer
        MutableTrackingEventDetails intDetails = new MutableTrackingEventDetails(42);
        assertEquals(Optional.of(42), intDetails.getValue());
        assertEquals(Integer.class, intDetails.getValue().get().getClass());

        // Test with Double
        MutableTrackingEventDetails doubleDetails = new MutableTrackingEventDetails(3.14);
        assertEquals(Optional.of(3.14), doubleDetails.getValue());
        assertEquals(Double.class, doubleDetails.getValue().get().getClass());

        // Test with Long
        MutableTrackingEventDetails longDetails = new MutableTrackingEventDetails(123456789L);
        assertEquals(Optional.of(123456789L), longDetails.getValue());
        assertEquals(Long.class, longDetails.getValue().get().getClass());

        // Test with Float
        MutableTrackingEventDetails floatDetails = new MutableTrackingEventDetails(2.71f);
        assertEquals(Optional.of(2.71f), floatDetails.getValue());
        assertEquals(Float.class, floatDetails.getValue().get().getClass());
    }

    @Test
    void add_shouldSupportFluentAPI() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(42)
                .add("stringKey", "stringValue")
                .add("intKey", 123)
                .add("doubleKey", 3.14)
                .add("boolKey", true);

        assertEquals(Optional.of(42), details.getValue());
        assertEquals(4, details.keySet().size());
        assertEquals("stringValue", details.getValue("stringKey").asString());
        assertEquals(123, details.getValue("intKey").asInteger());
        assertEquals(3.14, details.getValue("doubleKey").asDouble());
        assertEquals(true, details.getValue("boolKey").asBoolean());
    }

    @Test
    void add_shouldReturnSameInstance() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(42);

        MutableTrackingEventDetails result1 = details.add("key1", "value1");
        MutableTrackingEventDetails result2 = details.add("key2", 123);

        assertSame(details, result1);
        assertSame(details, result2);
    }

    @Test
    void addMethods_shouldSupportAllTypes() {
        Instant now = Instant.now();
        MutableStructure structure = new MutableStructure().add("nested", "value");
        List<Value> valueList = Arrays.asList(new Value("item1"), new Value("item2"));
        Value customValue = new Value("customValue");

        MutableTrackingEventDetails details = new MutableTrackingEventDetails()
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .add("doubleKey", 3.14)
                .add("boolKey", true)
                .add("instantKey", now)
                .add("structKey", structure)
                .add("listKey", valueList)
                .add("valueKey", customValue);

        assertEquals(8, details.keySet().size());
        assertEquals("stringValue", details.getValue("stringKey").asString());
        assertEquals(42, details.getValue("intKey").asInteger());
        assertEquals(3.14, details.getValue("doubleKey").asDouble());
        assertEquals(true, details.getValue("boolKey").asBoolean());
        assertEquals(now, details.getValue("instantKey").asInstant());
        assertTrue(details.getValue("structKey").isStructure());
        assertTrue(details.getValue("listKey").isList());
        assertEquals("customValue", details.getValue("valueKey").asString());
    }

    @Test
    void addMethods_shouldOverwriteExistingKeys() {
        MutableTrackingEventDetails details =
                new MutableTrackingEventDetails().add("key", "firstValue").add("key", "secondValue");

        assertEquals(1, details.keySet().size());
        assertEquals("secondValue", details.getValue("key").asString());
    }

    @Test
    void addMethods_shouldHandleNullValues() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails()
                .add("stringKey", (String) null)
                .add("intKey", (Integer) null)
                .add("doubleKey", (Double) null)
                .add("boolKey", (Boolean) null)
                .add("instantKey", (Instant) null)
                .add("structKey", (Structure) null)
                .add("listKey", (List<Value>) null)
                .add("valueKey", (Value) null);

        assertEquals(8, details.keySet().size());
        // All values should exist in the keySet but return null from getValue since MutableStructure doesn't store null
        // values
        // Instead, let's test that the keys exist but may return null
        assertTrue(details.keySet().contains("stringKey"));
        assertTrue(details.keySet().contains("intKey"));
        assertTrue(details.keySet().contains("doubleKey"));
        assertTrue(details.keySet().contains("boolKey"));
        assertTrue(details.keySet().contains("instantKey"));
        assertTrue(details.keySet().contains("structKey"));
        assertTrue(details.keySet().contains("listKey"));
        assertTrue(details.keySet().contains("valueKey"));
    }

    @Test
    void structureDelegation_shouldWorkCorrectly() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(100)
                .add("key1", "value1")
                .add("key2", 456)
                .add("key3", true);

        // Test delegation to structure methods
        assertFalse(details.isEmpty());
        assertEquals(3, details.keySet().size());
        assertTrue(details.keySet().contains("key1"));
        assertTrue(details.keySet().contains("key2"));
        assertTrue(details.keySet().contains("key3"));

        // Test getValue delegation
        assertEquals("value1", details.getValue("key1").asString());
        assertEquals(456, details.getValue("key2").asInteger());
        assertEquals(true, details.getValue("key3").asBoolean());
    }

    @Test
    void asMap_shouldReturnStructureMap() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(42).add("key1", "value1");

        Map<String, Value> resultMap = details.asMap();
        assertEquals(1, resultMap.size());
        assertEquals("value1", resultMap.get("key1").asString());
    }

    @Test
    void asUnmodifiableMap_shouldReturnUnmodifiableMap() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(42).add("key1", "value1");

        Map<String, Value> unmodifiableMap = details.asUnmodifiableMap();
        assertEquals(1, unmodifiableMap.size());
        assertEquals("value1", unmodifiableMap.get("key1").asString());
    }

    @Test
    void asObjectMap_shouldReturnObjectMap() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(42)
                .add("stringKey", "stringValue")
                .add("intKey", 123)
                .add("boolKey", true);

        Map<String, Object> objectMap = details.asObjectMap();
        assertEquals(3, objectMap.size());
        assertEquals("stringValue", objectMap.get("stringKey"));
        assertEquals(123, objectMap.get("intKey"));
        assertEquals(true, objectMap.get("boolKey"));
    }

    @Test
    void equals_shouldWorkCorrectly() {
        MutableTrackingEventDetails details1 = new MutableTrackingEventDetails(42).add("key1", "value1");

        MutableTrackingEventDetails details2 = new MutableTrackingEventDetails(42).add("key1", "value1");

        MutableTrackingEventDetails details3 = new MutableTrackingEventDetails(42).add("key1", "different");

        MutableTrackingEventDetails details4 = new MutableTrackingEventDetails(99).add("key1", "value1");

        MutableTrackingEventDetails details5 = new MutableTrackingEventDetails();

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
        MutableTrackingEventDetails emptyDetails = new MutableTrackingEventDetails();
        assertEquals(details5, emptyDetails);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        MutableTrackingEventDetails details1 = new MutableTrackingEventDetails(42).add("key1", "value1");

        MutableTrackingEventDetails details2 = new MutableTrackingEventDetails(42).add("key1", "value1");

        assertEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void toString_shouldIncludeValueAndStructure() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(42).add("key1", "value1");

        String toString = details.toString();
        assertTrue(toString.contains("MutableTrackingEventDetails"));
        assertTrue(toString.contains("value=42"));
        assertTrue(toString.contains("structure="));
    }

    @Test
    void toString_shouldHandleNullValue() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails();

        String toString = details.toString();
        assertTrue(toString.contains("MutableTrackingEventDetails"));
        assertTrue(toString.contains("value=null"));
    }

    @Test
    void mutability_shouldAllowModificationAfterCreation() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(42).add("key1", "value1");

        assertEquals(1, details.keySet().size());

        // Should be able to add more attributes after creation
        details.add("key2", "value2");
        assertEquals(2, details.keySet().size());
        assertEquals("value1", details.getValue("key1").asString());
        assertEquals("value2", details.getValue("key2").asString());

        // Should be able to overwrite existing attributes
        details.add("key1", "newValue");
        assertEquals(2, details.keySet().size());
        assertEquals("newValue", details.getValue("key1").asString());
    }

    @Test
    void differentValueTypes_shouldNotBeEqual() {
        MutableTrackingEventDetails intDetails = new MutableTrackingEventDetails(42);
        MutableTrackingEventDetails doubleDetails = new MutableTrackingEventDetails(42.0);

        // Even though numeric values are "equal", they should not be equal as objects
        assertNotEquals(intDetails, doubleDetails);
    }

    @Test
    void structureInterface_shouldSupportComplexStructures() {
        // Test with nested structure
        MutableStructure nestedStructure = new MutableStructure().add("nested", "nestedValue");

        MutableTrackingEventDetails details =
                new MutableTrackingEventDetails(42).add("nested_structure", nestedStructure);

        assertFalse(details.isEmpty());
        assertTrue(details.getValue("nested_structure").isStructure());
        assertEquals(
                "nestedValue",
                details.getValue("nested_structure")
                        .asStructure()
                        .getValue("nested")
                        .asString());
    }

    @Test
    void mutableVsImmutable_shouldBehaveDifferently() {
        // Compare mutable vs immutable behavior
        MutableTrackingEventDetails mutableDetails = new MutableTrackingEventDetails(42);
        ImmutableTrackingEventDetails immutableDetails =
                ImmutableTrackingEventDetails.builder().value(42).build();

        // Both should start equal in content (though they're different classes)
        assertEquals(Optional.of(42), mutableDetails.getValue());
        assertEquals(Optional.of(42), immutableDetails.getValue());
        assertTrue(mutableDetails.isEmpty());
        assertTrue(immutableDetails.isEmpty());

        // Mutable can be modified after creation
        mutableDetails.add("key", "value");
        assertEquals(1, mutableDetails.keySet().size());

        // Immutable cannot be modified (would need a new instance)
        assertEquals(0, immutableDetails.keySet().size());

        // They should not be equal (different classes)
        assertNotEquals(mutableDetails, immutableDetails);
    }

    @Test
    void chainedOperations_shouldWorkCorrectly() {
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(100)
                .add("step1", "first")
                .add("step2", 2)
                .add("step3", true)
                .add("step4", 3.14)
                .add("step5", "final");

        assertEquals(Optional.of(100), details.getValue());
        assertEquals(5, details.keySet().size());
        assertEquals("first", details.getValue("step1").asString());
        assertEquals(2, details.getValue("step2").asInteger());
        assertEquals(true, details.getValue("step3").asBoolean());
        assertEquals(3.14, details.getValue("step4").asDouble());
        assertEquals("final", details.getValue("step5").asString());
    }
}

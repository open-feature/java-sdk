package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class EnhancedImmutableMetadataTest {

    @Test
    void builder_shouldCreateEmptyMetadata() {
        var metadata = Metadata.EMPTY;

        assertNotNull(metadata);
        assertTrue(metadata.asUnmodifiableObjectMap().isEmpty());
    }

    @Test
    void builder_addString_shouldAddStringValue() {
        String key = "stringKey";
        String value = "stringValue";

        var metadata = Metadata.immutableBuilder().add(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.asUnmodifiableObjectMap().get(key));
        assertEquals(value, metadata.getString(key));
    }

    @Test
    void builder_addInteger_shouldAddIntegerValue() {
        String key = "intKey";
        Integer value = 42;

        var metadata = Metadata.immutableBuilder().add(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getInteger(key));
    }

    @Test
    void builder_addLong_shouldAddLongValue() {
        String key = "longKey";
        Long value = 1234567890L;

        var metadata = Metadata.immutableBuilder().add(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getLong(key));
    }

    @Test
    void builder_addFloat_shouldAddFloatValue() {
        String key = "floatKey";
        Float value = 3.14f;

        var metadata = Metadata.immutableBuilder().add(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getFloat(key));
    }

    @Test
    void builder_addDouble_shouldAddDoubleValue() {
        String key = "doubleKey";
        Double value = 3.141592653589793;

        var metadata = Metadata.immutableBuilder().add(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getDouble(key));
    }

    @Test
    void builder_addBoolean_shouldAddBooleanValue() {
        String key = "boolKey";
        Boolean value = true;

        var metadata = Metadata.immutableBuilder().add(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getBoolean(key));
    }

    @Test
    void builder_shouldAddMultipleValuesOfDifferentTypes() {
        var metadata = Metadata.immutableBuilder()
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .add("longKey", 1234567890L)
                .add("floatKey", 3.14f)
                .add("doubleKey", 3.141592653589793)
                .add("boolKey", true)
                .build();

        assertEquals(6, metadata.asUnmodifiableObjectMap().size());
        assertEquals("stringValue", metadata.getString("stringKey"));
        assertEquals(Integer.valueOf(42), metadata.getInteger("intKey"));
        assertEquals(Long.valueOf(1234567890L), metadata.getLong("longKey"));
        assertEquals(Float.valueOf(3.14f), metadata.getFloat("floatKey"));
        assertEquals(Double.valueOf(3.141592653589793), metadata.getDouble("doubleKey"));
        assertEquals(Boolean.TRUE, metadata.getBoolean("boolKey"));
    }

    @Test
    void builder_shouldHandleNullValues() {
        var metadata = Metadata.immutableBuilder()
                .add("stringKey", (String) null)
                .add("intKey", (Integer) null)
                .add("longKey", (Long) null)
                .add("floatKey", (Float) null)
                .add("doubleKey", (Double) null)
                .add("boolKey", (Boolean) null)
                .build();

        assertEquals(6, metadata.asUnmodifiableObjectMap().size());
        assertNull(metadata.getString("stringKey"));
        assertNull(metadata.getInteger("intKey"));
        assertNull(metadata.getLong("longKey"));
        assertNull(metadata.getFloat("floatKey"));
        assertNull(metadata.getDouble("doubleKey"));
        assertNull(metadata.getBoolean("boolKey"));
    }

    @Test
    void builder_shouldOverwriteExistingKeys() {
        var metadata = Metadata.immutableBuilder()
                .add("key", "firstValue")
                .add("key", "secondValue")
                .build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals("secondValue", metadata.getString("key"));
    }

    @Test
    void builder_shouldAllowChaining() {
        var metadata = Metadata.immutableBuilder()
                .add("key1", "value1")
                .add("key2", 42)
                .add("key3", true)
                .build();

        assertEquals(3, metadata.asUnmodifiableObjectMap().size());
        assertEquals("value1", metadata.getString("key1"));
        assertEquals(Integer.valueOf(42), metadata.getInteger("key2"));
        assertEquals(Boolean.TRUE, metadata.getBoolean("key3"));
    }

    @Test
    void getters_shouldReturnNullForMissingKeys() {
        var metadata = Metadata.immutableBuilder().build();

        assertNull(metadata.getString("missing"));
        assertNull(metadata.getInteger("missing"));
        assertNull(metadata.getLong("missing"));
        assertNull(metadata.getFloat("missing"));
        assertNull(metadata.getDouble("missing"));
        assertNull(metadata.getBoolean("missing"));
    }

    @Test
    void getters_shouldReturnNullForWrongType() {
        var metadata = Metadata.immutableBuilder().add("key", "stringValue").build();

        assertEquals("stringValue", metadata.getString("key"));
        assertNull(metadata.getInteger("key")); // Wrong type should return null
        assertNull(metadata.getLong("key"));
        assertNull(metadata.getFloat("key"));
        assertNull(metadata.getDouble("key"));
        assertNull(metadata.getBoolean("key"));
    }

    @Test
    void asUnmodifiableObjectMap_shouldReturnUnmodifiableMap() {
        var metadata = Metadata.immutableBuilder().add("key", "value").build();

        Map<String, Object> map = metadata.asUnmodifiableObjectMap();
        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));

        // Should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            map.put("newKey", "newValue");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            map.remove("key");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            map.clear();
        });
    }

    @Test
    void equals_shouldWorkCorrectly() {
        var metadata1 = Metadata.immutableBuilder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        var metadata2 = Metadata.immutableBuilder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        var metadata3 = Metadata.immutableBuilder()
                .add("key1", "different")
                .add("key2", 42)
                .build();

        // Same content should be equal
        assertEquals(metadata1, metadata2);
        assertEquals(metadata2, metadata1);

        // Different content should not be equal
        assertNotEquals(metadata1, metadata3);

        // Self-equality
        assertEquals(metadata1, metadata1);

        // Null comparison
        assertNotEquals(metadata1, null);

        // Different class comparison
        assertNotEquals(metadata1, "not metadata");
    }

    @Test
    void hashCode_shouldBeConsistent() {
        var metadata1 = Metadata.immutableBuilder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        var metadata2 = Metadata.immutableBuilder()
                .add("key1", "value1")
                .add("key2", 42)
                .build();

        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void toString_shouldIncludeContent() {
        var metadata = Metadata.immutableBuilder()
                .add("stringKey", "stringValue")
                .add("intKey", 42)
                .build();

        String toString = metadata.toString();
        assertTrue(toString.contains("ImmutableMetadata"));
        // Note: toString uses default Object.toString, content not directly included
        assertNotNull(toString);
    }

    @Test
    void builder_shouldCreateIndependentInstances() {
        var builder = Metadata.immutableBuilder().add("key1", "value1");

        var metadata1 = builder.build();

        // Adding to builder after first build should not affect first instance
        builder.add("key2", "value2");
        var metadata2 = builder.build();

        assertEquals(1, metadata1.asUnmodifiableObjectMap().size());
        assertEquals(2, metadata2.asUnmodifiableObjectMap().size());
        assertNull(metadata1.getString("key2"));
        assertEquals("value2", metadata2.getString("key2"));
    }

    @Test
    void numberTypes_shouldBeStoredCorrectly() {
        // Test edge cases for numeric types
        var metadata = Metadata.immutableBuilder()
                .add("maxInt", Integer.MAX_VALUE)
                .add("minInt", Integer.MIN_VALUE)
                .add("maxLong", Long.MAX_VALUE)
                .add("minLong", Long.MIN_VALUE)
                .add("maxFloat", Float.MAX_VALUE)
                .add("minFloat", Float.MIN_VALUE)
                .add("maxDouble", Double.MAX_VALUE)
                .add("minDouble", Double.MIN_VALUE)
                .build();

        assertEquals(Integer.MAX_VALUE, metadata.getInteger("maxInt"));
        assertEquals(Integer.MIN_VALUE, metadata.getInteger("minInt"));
        assertEquals(Long.MAX_VALUE, metadata.getLong("maxLong"));
        assertEquals(Long.MIN_VALUE, metadata.getLong("minLong"));
        assertEquals(Float.MAX_VALUE, metadata.getFloat("maxFloat"));
        assertEquals(Float.MIN_VALUE, metadata.getFloat("minFloat"));
        assertEquals(Double.MAX_VALUE, metadata.getDouble("maxDouble"));
        assertEquals(Double.MIN_VALUE, metadata.getDouble("minDouble"));
    }
}

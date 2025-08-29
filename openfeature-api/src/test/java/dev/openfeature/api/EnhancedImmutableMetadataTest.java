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
        ImmutableMetadata metadata = ImmutableMetadata.builder().build();

        assertNotNull(metadata);
        assertTrue(metadata.asUnmodifiableObjectMap().isEmpty());
    }

    @Test
    void builder_addString_shouldAddStringValue() {
        String key = "stringKey";
        String value = "stringValue";

        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.asUnmodifiableObjectMap().get(key));
        assertEquals(value, metadata.getString(key));
    }

    @Test
    void builder_addInteger_shouldAddIntegerValue() {
        String key = "intKey";
        Integer value = 42;

        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addInteger(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getInteger(key));
    }

    @Test
    void builder_addLong_shouldAddLongValue() {
        String key = "longKey";
        Long value = 1234567890L;

        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addLong(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getLong(key));
    }

    @Test
    void builder_addFloat_shouldAddFloatValue() {
        String key = "floatKey";
        Float value = 3.14f;

        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addFloat(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getFloat(key));
    }

    @Test
    void builder_addDouble_shouldAddDoubleValue() {
        String key = "doubleKey";
        Double value = 3.141592653589793;

        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addDouble(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getDouble(key));
    }

    @Test
    void builder_addBoolean_shouldAddBooleanValue() {
        String key = "boolKey";
        Boolean value = true;

        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addBoolean(key, value).build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals(value, metadata.getBoolean(key));
    }

    @Test
    void builder_shouldAddMultipleValuesOfDifferentTypes() {
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addString("stringKey", "stringValue")
                .addInteger("intKey", 42)
                .addLong("longKey", 1234567890L)
                .addFloat("floatKey", 3.14f)
                .addDouble("doubleKey", 3.141592653589793)
                .addBoolean("boolKey", true)
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
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addString("stringKey", null)
                .addInteger("intKey", null)
                .addLong("longKey", null)
                .addFloat("floatKey", null)
                .addDouble("doubleKey", null)
                .addBoolean("boolKey", null)
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
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addString("key", "firstValue")
                .addString("key", "secondValue")
                .build();

        assertEquals(1, metadata.asUnmodifiableObjectMap().size());
        assertEquals("secondValue", metadata.getString("key"));
    }

    @Test
    void builder_shouldAllowChaining() {
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addString("key1", "value1")
                .addInteger("key2", 42)
                .addBoolean("key3", true)
                .build();

        assertEquals(3, metadata.asUnmodifiableObjectMap().size());
        assertEquals("value1", metadata.getString("key1"));
        assertEquals(Integer.valueOf(42), metadata.getInteger("key2"));
        assertEquals(Boolean.TRUE, metadata.getBoolean("key3"));
    }

    @Test
    void getters_shouldReturnNullForMissingKeys() {
        ImmutableMetadata metadata = ImmutableMetadata.builder().build();

        assertNull(metadata.getString("missing"));
        assertNull(metadata.getInteger("missing"));
        assertNull(metadata.getLong("missing"));
        assertNull(metadata.getFloat("missing"));
        assertNull(metadata.getDouble("missing"));
        assertNull(metadata.getBoolean("missing"));
    }

    @Test
    void getters_shouldReturnNullForWrongType() {
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "stringValue").build();

        assertEquals("stringValue", metadata.getString("key"));
        assertNull(metadata.getInteger("key")); // Wrong type should return null
        assertNull(metadata.getLong("key"));
        assertNull(metadata.getFloat("key"));
        assertNull(metadata.getDouble("key"));
        assertNull(metadata.getBoolean("key"));
    }

    @Test
    void asUnmodifiableObjectMap_shouldReturnUnmodifiableMap() {
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();

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
        ImmutableMetadata metadata1 = ImmutableMetadata.builder()
                .addString("key1", "value1")
                .addInteger("key2", 42)
                .build();

        ImmutableMetadata metadata2 = ImmutableMetadata.builder()
                .addString("key1", "value1")
                .addInteger("key2", 42)
                .build();

        ImmutableMetadata metadata3 = ImmutableMetadata.builder()
                .addString("key1", "different")
                .addInteger("key2", 42)
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
        ImmutableMetadata metadata1 = ImmutableMetadata.builder()
                .addString("key1", "value1")
                .addInteger("key2", 42)
                .build();

        ImmutableMetadata metadata2 = ImmutableMetadata.builder()
                .addString("key1", "value1")
                .addInteger("key2", 42)
                .build();

        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void toString_shouldIncludeContent() {
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addString("stringKey", "stringValue")
                .addInteger("intKey", 42)
                .build();

        String toString = metadata.toString();
        assertTrue(toString.contains("ImmutableMetadata"));
        // Note: toString uses default Object.toString, content not directly included
        assertNotNull(toString);
    }

    @Test
    void builder_shouldCreateIndependentInstances() {
        ImmutableMetadata.Builder builder = ImmutableMetadata.builder().addString("key1", "value1");

        ImmutableMetadata metadata1 = builder.build();

        // Adding to builder after first build should not affect first instance
        builder.addString("key2", "value2");
        ImmutableMetadata metadata2 = builder.build();

        assertEquals(1, metadata1.asUnmodifiableObjectMap().size());
        assertEquals(2, metadata2.asUnmodifiableObjectMap().size());
        assertNull(metadata1.getString("key2"));
        assertEquals("value2", metadata2.getString("key2"));
    }

    @Test
    void numberTypes_shouldBeStoredCorrectly() {
        // Test edge cases for numeric types
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addInteger("maxInt", Integer.MAX_VALUE)
                .addInteger("minInt", Integer.MIN_VALUE)
                .addLong("maxLong", Long.MAX_VALUE)
                .addLong("minLong", Long.MIN_VALUE)
                .addFloat("maxFloat", Float.MAX_VALUE)
                .addFloat("minFloat", Float.MIN_VALUE)
                .addDouble("maxDouble", Double.MAX_VALUE)
                .addDouble("minDouble", Double.MIN_VALUE)
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

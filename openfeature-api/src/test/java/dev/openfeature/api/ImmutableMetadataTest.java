package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImmutableMetadataTest {
    @Test
    void unequalImmutableMetadataAreUnequal() {
        var i1 = Metadata.immutableBuilder().add("key1", "value1").build();
        var i2 = Metadata.immutableBuilder().add("key1", "value2").build();

        assertNotEquals(i1, i2);
    }

    @Test
    void equalImmutableMetadataAreEqual() {
        var i1 = Metadata.immutableBuilder().add("key1", "value1").build();
        var i2 = Metadata.immutableBuilder().add("key1", "value1").build();

        assertEquals(i1, i2);
    }

    @Test
    void retrieveAsUnmodifiableMap() {
        var metadata = Metadata.immutableBuilder().add("key1", "value1").build();

        Map<String, Object> unmodifiableMap = metadata.asUnmodifiableObjectMap();
        assertEquals(unmodifiableMap.size(), 1);
        assertEquals(unmodifiableMap.get("key1"), "value1");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> unmodifiableMap.put("key3", "value3"));
    }
}

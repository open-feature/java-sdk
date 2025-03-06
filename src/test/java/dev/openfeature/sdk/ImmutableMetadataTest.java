package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ImmutableMetadataTest {
    @Test
    void unequalImmutableMetadataAreUnequal() {
        ImmutableMetadata i1 = ImmutableMetadata.builder().addString("key1", "value1").build();
        ImmutableMetadata i2 = ImmutableMetadata.builder().addString("key1", "value2").build();

        assertNotEquals(i1, i2);
    }

    @Test
    void equalImmutableMetadataAreEqual() {
        ImmutableMetadata i1 = ImmutableMetadata.builder().addString("key1", "value1").build();
        ImmutableMetadata i2 = ImmutableMetadata.builder().addString("key1", "value1").build();

        assertEquals(i1, i2);
    }
}

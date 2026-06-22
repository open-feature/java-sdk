package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImmutableMetadataTest {
    @Test
    void unequalImmutableMetadataAreUnequal() {
        ImmutableMetadata i1 =
                ImmutableMetadata.builder().addString("key1", "value1").build();
        ImmutableMetadata i2 =
                ImmutableMetadata.builder().addString("key1", "value2").build();

        assertNotEquals(i1, i2);
    }

    @Test
    void equalImmutableMetadataAreEqual() {
        ImmutableMetadata i1 =
                ImmutableMetadata.builder().addString("key1", "value1").build();
        ImmutableMetadata i2 =
                ImmutableMetadata.builder().addString("key1", "value1").build();

        assertEquals(i1, i2);
    }

    @Test
    void retrieveAsUnmodifiableMap() {
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key1", "value1").build();

        Map<String, Object> unmodifiableMap = metadata.asUnmodifiableMap();
        assertEquals(unmodifiableMap.size(), 1);
        assertEquals(unmodifiableMap.get("key1"), "value1");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> unmodifiableMap.put("key3", "value3"));
    }

    @Test
    @DisplayName("empty builder returns EMPTY singleton")
    void emptyBuilderReturnsEmptySingleton() {
        assertThat(ImmutableMetadata.builder().build()).isSameAs(ImmutableMetadata.EMPTY);
    }

    @Test
    @DisplayName("asUnmodifiableMap on empty metadata returns empty unmodifiable map")
    void asUnmodifiableMapOnEmptyMetadataIsUnmodifiable() {
        Map<String, Object> map = ImmutableMetadata.EMPTY.asUnmodifiableMap();
        assertThat(map).isEmpty();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> map.put("key", "value"));
    }
}

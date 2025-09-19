package dev.openfeature.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.types.Metadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetadataTest {

    @Test
    @DisplayName("Test metadata payload construction and retrieval")
    void builder_validation() {
        // given
        var flagMetadata = Metadata.immutableBuilder()
                .add("string", "string")
                .add("integer", 1)
                .add("long", 1L)
                .add("float", 1.5f)
                .add("double", Double.MAX_VALUE)
                .add("boolean", Boolean.FALSE)
                .build();

        // then
        assertThat(flagMetadata.getString("string")).isEqualTo("string");
        assertThat(flagMetadata.getValue("string", String.class)).isEqualTo("string");

        assertThat(flagMetadata.getInteger("integer")).isEqualTo(1);
        assertThat(flagMetadata.getValue("integer", Integer.class)).isEqualTo(1);

        assertThat(flagMetadata.getLong("long")).isEqualTo(1L);
        assertThat(flagMetadata.getValue("long", Long.class)).isEqualTo(1L);

        assertThat(flagMetadata.getFloat("float")).isEqualTo(1.5f);
        assertThat(flagMetadata.getValue("float", Float.class)).isEqualTo(1.5f);

        assertThat(flagMetadata.getDouble("double")).isEqualTo(Double.MAX_VALUE);
        assertThat(flagMetadata.getValue("double", Double.class)).isEqualTo(Double.MAX_VALUE);

        assertThat(flagMetadata.getBoolean("boolean")).isEqualTo(Boolean.FALSE);
        assertThat(flagMetadata.getValue("boolean", Boolean.class)).isEqualTo(Boolean.FALSE);
    }

    @Test
    @DisplayName("Value type mismatch returns a null")
    void value_type_validation() {
        // given
        var flagMetadata = Metadata.immutableBuilder().add("string", "string").build();

        // then
        assertThat(flagMetadata.getBoolean("string")).isNull();
    }

    @Test
    @DisplayName("A null is returned if key does not exist")
    void notfound_error_validation() {
        // given
        var flagMetadata = Metadata.immutableBuilder().build();

        // then
        assertThat(flagMetadata.getBoolean("string")).isNull();
    }

    @Test
    @DisplayName("isEmpty and isNotEmpty return correctly when the metadata is empty")
    void isEmpty_isNotEmpty_return_correctly_when_metadata_is_empty() {
        // given
        var flagMetadata = Metadata.immutableBuilder().build();

        // then
        assertTrue(flagMetadata.isEmpty());
        assertFalse(flagMetadata.isNotEmpty());
    }

    @Test
    @DisplayName("isEmpty and isNotEmpty return correctly when the metadata is not empty")
    void isEmpty_isNotEmpty_return_correctly_when_metadata_is_not_empty() {
        // given
        var flagMetadata = Metadata.immutableBuilder().add("a", "b").build();

        // then
        assertFalse(flagMetadata.isEmpty());
        assertTrue(flagMetadata.isNotEmpty());
    }
}

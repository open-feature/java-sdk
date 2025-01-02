package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlagMetadataTest {

    @Test
    @DisplayName("Test metadata payload construction and retrieval")
    public void builder_validation() {
        // given
        ImmutableMetadata flagMetadata = ImmutableMetadata.builder()
                .addString("string", "string")
                .addInteger("integer", 1)
                .addLong("long", 1L)
                .addFloat("float", 1.5f)
                .addDouble("double", Double.MAX_VALUE)
                .addBoolean("boolean", Boolean.FALSE)
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
    public void value_type_validation() {
        // given
        ImmutableMetadata flagMetadata =
                ImmutableMetadata.builder().addString("string", "string").build();

        // then
        assertThat(flagMetadata.getBoolean("string")).isNull();
    }

    @Test
    @DisplayName("A null is returned if key does not exist")
    public void notfound_error_validation() {
        // given
        ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();

        // then
        assertThat(flagMetadata.getBoolean("string")).isNull();
    }
}

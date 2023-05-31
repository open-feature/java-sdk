package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.ParseError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlagMetadataTest {

    @Test
    public void builder_validation() {
        // given
        FlagMetadata flagMetadata = FlagMetadata.builder()
                .addString("string", "string")
                .addInteger("integer", 1)
                .addFloat("float", 1.5f)
                .addDouble("double", Double.MAX_VALUE)
                .addBoolean("boolean", Boolean.FALSE)
                .build();

        // then
        assertThat(flagMetadata.getString("string")).isEqualTo("string");
        assertThat(flagMetadata.getInteger("integer")).isEqualTo(1);
        assertThat(flagMetadata.getFloat("float")).isEqualTo(1.5f);
        assertThat(flagMetadata.getDouble("double")).isEqualTo(Double.MAX_VALUE);
        assertThat(flagMetadata.getBoolean("boolean")).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void parse_error_validation() {
        // given
        FlagMetadata flagMetadata = FlagMetadata.builder()
                .addString("string", "string")
                .build();

        // then
        assertThatThrownBy(() -> flagMetadata.getBoolean("string")).isInstanceOf(ParseError.class);
    }

    @Test
    public void notfound_error_validation() {
        // given
        FlagMetadata flagMetadata = FlagMetadata.builder().build();

        // then
        assertThatThrownBy(() -> flagMetadata.getBoolean("string")).isInstanceOf(GeneralError.class);
    }
}
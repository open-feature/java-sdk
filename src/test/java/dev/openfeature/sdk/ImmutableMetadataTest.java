package dev.openfeature.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImmutableMetadataTest {

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
        ImmutableMetadata flagMetadata = ImmutableMetadata.builder()
                .addString("string", "string")
                .build();

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

    @Test
    @DisplayName("Make sure class is se/deserializable")
    public void test_serialize_deserialization() throws JsonProcessingException {
        // given
        ImmutableMetadata original = ImmutableMetadata.builder()
                .addString("string", "string")
                .addInteger("integer", 1)
                .addLong("long", Long.MAX_VALUE)
                .addFloat("float", Float.MAX_VALUE)
                .addDouble("double", Double.MAX_VALUE)
                .addBoolean("boolean", Boolean.FALSE)
                .build();


        final ObjectMapper mapper = new ObjectMapper();

        // when
        final String json = mapper.writeValueAsString(original);
        final ImmutableMetadata converted = mapper.readValue(json, ImmutableMetadata.class);

        // then
        assertThat(json).isEqualTo("{\"metadata\":{\"boolean\":false,\"string\":\"string\"," +
                "\"double\":1.7976931348623157E308,\"integer\":1,\"float\":3.4028235E38,\"long\":9223372036854775807}}");

        assertThat(converted.getValue("string", String.class)).isEqualTo(original.getString("string"));
        assertThat(converted.getValue("integer", Integer.class)).isEqualTo(original.getInteger("integer"));
        assertThat(converted.getValue("boolean", Boolean.class)).isEqualTo(original.getBoolean("boolean"));
        assertThat(converted.getValue("long", Long.class)).isEqualTo(original.getValue("long", Long.class));
        assertThat(converted.getValue("double", Number.class)).isEqualTo(original.getValue("double", Number.class));

        // float get converted to double, hence representation comparison
        assertThat(converted.getValue("float", Double.class).toString())
                .isEqualTo(original.getValue("float", Float.class).toString());
    }
}

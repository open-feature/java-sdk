package dev.openfeature.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderEvaluationTest {

    @Test
    @DisplayName("Provider evaluation must support JSON se/deserialization")
    public void test_serialization_deserialization() throws IOException {
        // given
        final ProviderEvaluation<String> original = ProviderEvaluation.<String>builder()
                .reason(Reason.STATIC.toString())
                .variant("Default")
                .value("StringValue")
                .flagMetadata(ImmutableMetadata.builder()
                        .addString("key", "value")
                        .addInteger("integer", 10)
                        .build()
                )
                .build();


        // when
        final String json = new ObjectMapper().writer().writeValueAsString(original);
        final ObjectMapper objectMapper = new ObjectMapper();
        final Reader reader = new StringReader(json);
        final ProviderEvaluation converted = objectMapper.readValue(reader, ProviderEvaluation.class);

        // then
        assertThat(converted.getReason()).isEqualTo(original.getReason());
        assertThat(converted.getVariant()).isEqualTo(original.getVariant());
        assertThat(converted.getValue()).isEqualTo(original.getValue());
        assertThat(converted.getFlagMetadata().getString("key"))
                .isEqualTo(original.getFlagMetadata().getString("key"));
        assertThat(converted.getFlagMetadata().getInteger("integer"))
                .isEqualTo(original.getFlagMetadata().getInteger("integer"));
    }
}
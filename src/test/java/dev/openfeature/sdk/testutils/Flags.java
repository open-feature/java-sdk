package dev.openfeature.sdk.testutils;

import io.cucumber.core.internal.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.cucumber.core.internal.com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Flags {

    public static class FlagsBuilder {

        private String configurationJson;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private FlagsBuilder() {

        }

        public FlagsBuilder setConfigurationJson(String configurationJson) {
            this.configurationJson = configurationJson;
            return this;
        }

        public Flags build() throws JsonProcessingException {
            return objectMapper.readValue(configurationJson, Flags.class);
        }

    }

    public static FlagsBuilder builder() {
        return new FlagsBuilder();
    }

    private Map<String, Flag> flags;

    public enum State {
        ENABLED, DISABLED
    }
}

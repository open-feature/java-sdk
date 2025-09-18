package dev.openfeature.sdk.testutils.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import dev.openfeature.sdk.providers.memory.Flag;
import java.util.Map;

@JsonDeserialize(builder = Flag.Builder.class)
@SuppressWarnings("rawtypes")
public abstract class InMemoryFlagMixin {

    @JsonPOJOBuilder(withPrefix = "")
    public abstract class FlagBuilderMixin {

        @JsonProperty("variants")
        @JsonDeserialize(using = VariantsMapDeserializer.class)
        public abstract Flag.Builder variants(Map<String, ?> variants);
    }
}

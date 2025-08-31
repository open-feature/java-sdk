package dev.openfeature.sdk.testutils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dev.openfeature.sdk.providers.memory.ContextEvaluator;
import java.io.IOException;

public class ContextEvaluatorDeserializer extends JsonDeserializer<ContextEvaluator<?>> {
    @Override
    public ContextEvaluator<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isTextual()) {
            return new CelContextEvaluator<>(node.asText());
        }

        if (node.isObject() && node.has("expression")) {
            return new CelContextEvaluator<>(node.get("expression").asText());
        }

        return null;
    }
}

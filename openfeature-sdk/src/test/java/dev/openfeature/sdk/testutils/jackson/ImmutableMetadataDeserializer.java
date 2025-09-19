package dev.openfeature.sdk.testutils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import dev.openfeature.api.types.ImmutableMetadataBuilder;
import dev.openfeature.api.types.Metadata;
import java.io.IOException;
import java.util.Map;

public class ImmutableMetadataDeserializer extends JsonDeserializer<Metadata> {
    @Override
    public Metadata deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, Object> properties = p.readValueAs(new TypeReference<Map<String, Object>>() {});

        ImmutableMetadataBuilder builder = Metadata.immutableBuilder();

        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    builder.add(key, (String) value);
                } else if (value instanceof Integer) {
                    builder.add(key, (Integer) value);
                } else if (value instanceof Long) {
                    builder.add(key, (Long) value);
                } else if (value instanceof Float) {
                    builder.add(key, (Float) value);
                } else if (value instanceof Double) {
                    builder.add(key, (Double) value);
                } else if (value instanceof Boolean) {
                    builder.add(key, (Boolean) value);
                }
            }
        }

        return builder.build();
    }
}

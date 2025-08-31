package dev.openfeature.sdk.testutils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import dev.openfeature.sdk.ImmutableMetadata;
import java.io.IOException;
import java.util.Map;

public class ImmutableMetadataDeserializer extends JsonDeserializer<ImmutableMetadata> {
    @Override
    public ImmutableMetadata deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, Object> properties = p.readValueAs(new TypeReference<Map<String, Object>>() {});

        ImmutableMetadata.ImmutableMetadataBuilder builder = ImmutableMetadata.builder();

        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    builder.addString(key, (String) value);
                } else if (value instanceof Integer) {
                    builder.addInteger(key, (Integer) value);
                } else if (value instanceof Long) {
                    builder.addLong(key, (Long) value);
                } else if (value instanceof Float) {
                    builder.addFloat(key, (Float) value);
                } else if (value instanceof Double) {
                    builder.addDouble(key, (Double) value);
                } else if (value instanceof Boolean) {
                    builder.addBoolean(key, (Boolean) value);
                }
            }
        }

        return builder.build();
    }
}

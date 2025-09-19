package dev.openfeature.sdk.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfeature.api.types.Value;
import java.util.Objects;

public final class Utils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Utils() {}

    public static Object convert(String value, String type) {
        if (Objects.equals(value, "null")) {
            return null;
        }
        switch (type.toLowerCase()) {
            case "boolean":
                return Boolean.parseBoolean(value);
            case "string":
                return value;
            case "integer":
                return Integer.parseInt(value);
            case "float":
            case "double":
                return Double.parseDouble(value);
            case "long":
                return Long.parseLong(value);
            case "object":
                try {
                    return Value.objectToValue(OBJECT_MAPPER.readValue(value, Object.class));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            default:
        }
        throw new RuntimeException("Unknown config type: " + type);
    }
}

package dev.openfeature.sdk.e2e;

import java.util.Objects;

public final class Utils {

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
        }
        throw new RuntimeException("Unknown config type: " + type);
    }
}

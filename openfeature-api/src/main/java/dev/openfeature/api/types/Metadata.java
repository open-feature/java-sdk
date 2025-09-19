package dev.openfeature.api.types;

import java.util.Map;
import java.util.Set;

/**
 * Flag Metadata representation.
 */
public interface Metadata extends Structure {

    Metadata EMPTY = new ImmutableMetadata();

    static ImmutableMetadataBuilder immutableBuilder() {
        return new ImmutableMetadata.Builder();
    }

    @Override
    Set<String> keySet();

    @Override
    Value getValue(String key);

    <T> T getValue(String key, Class<T> type);

    @Override
    Map<String, Value> asMap();

    String getString(String key);

    Integer getInteger(String key);

    Long getLong(String key);

    Float getFloat(String key);

    Double getDouble(String key);

    Boolean getBoolean(String key);

    Map<String, Object> asUnmodifiableObjectMap();

    boolean isNotEmpty();
}

package dev.openfeature.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable Flag Metadata representation. Implementation is backed by a {@link Map} and immutability is provided
 * through builder and accessors.
 */
public class ImmutableMetadata extends AbstractStructure {

    private static final Logger log = LoggerFactory.getLogger(ImmutableMetadata.class);

    private ImmutableMetadata(Map<String, Value> attributes) {
        super(attributes);
    }

    @Override
    public Set<String> keySet() {
        return attributes.keySet();
    }

    @Override
    public Value getValue(String key) {
        return attributes.get(key);
    }

    /**
     * Generic value retrieval for the given key.
     */
    public <T> T getValue(final String key, final Class<T> type) {
        Value value = getValue(key);
        if (value == null) {
            log.debug("Metadata key " + key + " does not exist");
            return null;
        }

        try {
            Object obj = value.asObject();
            return obj != null ? type.cast(obj) : null;
        } catch (ClassCastException e) {
            log.debug("Error retrieving value for key " + key, e);
            return null;
        }
    }

    @Override
    public Map<String, Value> asMap() {
        return new HashMap<>(attributes);
    }

    /**
     * Retrieve a {@link String} value for the given key. A {@code null} value is returned if the key does not exist
     * or if the value is of a different type.
     *
     * @param key flag metadata key to retrieve
     */
    public String getString(final String key) {
        Value value = getValue(key);
        return value != null && value.isString() ? value.asString() : null;
    }

    /**
     * Retrieve a {@link Integer} value for the given key. A {@code null} value is returned if the key does not exist
     * or if the value is of a different type.
     *
     * @param key flag metadata key to retrieve
     */
    public Integer getInteger(final String key) {
        Value value = getValue(key);
        if (value != null && value.isNumber()) {
            Object obj = value.asObject();
            if (obj instanceof Integer) {
                return (Integer) obj;
            }
        }
        return null;
    }

    /**
     * Retrieve a {@link Long} value for the given key. A {@code null} value is returned if the key does not exist
     * or if the value is of a different type.
     *
     * @param key flag metadata key to retrieve
     */
    public Long getLong(final String key) {
        Value value = getValue(key);
        if (value != null && value.isNumber()) {
            Object obj = value.asObject();
            if (obj instanceof Long) {
                return (Long) obj;
            }
        }
        return null;
    }

    /**
     * Retrieve a {@link Float} value for the given key. A {@code null} value is returned if the key does not exist
     * or if the value is of a different type.
     *
     * @param key flag metadata key to retrieve
     */
    public Float getFloat(final String key) {
        Value value = getValue(key);
        if (value != null && value.isNumber()) {
            Object obj = value.asObject();
            if (obj instanceof Float) {
                return (Float) obj;
            }
        }
        return null;
    }

    /**
     * Retrieve a {@link Double} value for the given key. A {@code null} value is returned if the key does not exist
     * or if the value is of a different type.
     *
     * @param key flag metadata key to retrieve
     */
    public Double getDouble(final String key) {
        Value value = getValue(key);
        if (value != null && value.isNumber()) {
            Object obj = value.asObject();
            if (obj instanceof Double) {
                return (Double) obj;
            }
        }
        return null;
    }

    /**
     * Retrieve a {@link Boolean} value for the given key. A {@code null} value is returned if the key does not exist
     * or if the value is of a different type.
     *
     * @param key flag metadata key to retrieve
     */
    public Boolean getBoolean(final String key) {
        Value value = getValue(key);
        return value != null && value.isBoolean() ? value.asBoolean() : null;
    }

    /**
     * Returns an unmodifiable map of metadata as primitive objects.
     * This provides backward compatibility for the original ImmutableMetadata API.
     */
    public Map<String, Object> asUnmodifiableObjectMap() {
        return Collections.unmodifiableMap(asObjectMap());
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Obtain a builder for {@link ImmutableMetadata}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Immutable builder for {@link ImmutableMetadata}.
     */
    public static class Builder {
        private final Map<String, Value> attributes;

        private Builder() {
            attributes = new HashMap<>();
        }

        /**
         * Add String value to the metadata.
         *
         * @param key   flag metadata key to add
         * @param value flag metadata value to add
         */
        public Builder addString(final String key, final String value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Integer value to the metadata.
         *
         * @param key   flag metadata key to add
         * @param value flag metadata value to add
         */
        public Builder addInteger(final String key, final Integer value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Long value to the metadata.
         *
         * @param key   flag metadata key to add
         * @param value flag metadata value to add
         */
        public Builder addLong(final String key, final Long value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Float value to the metadata.
         *
         * @param key   flag metadata key to add
         * @param value flag metadata value to add
         */
        public Builder addFloat(final String key, final Float value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Double value to the metadata.
         *
         * @param key   flag metadata key to add
         * @param value flag metadata value to add
         */
        public Builder addDouble(final String key, final Double value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Boolean value to the metadata.
         *
         * @param key   flag metadata key to add
         * @param value flag metadata value to add
         */
        public Builder addBoolean(final String key, final Boolean value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Retrieve {@link ImmutableMetadata} with provided key,value pairs.
         */
        public ImmutableMetadata build() {
            return new ImmutableMetadata(new HashMap<>(this.attributes));
        }
    }
}

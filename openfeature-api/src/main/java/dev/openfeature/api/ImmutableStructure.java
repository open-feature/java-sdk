package dev.openfeature.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ImmutableStructure} represents a potentially nested object type which
 * is used to represent
 * structured data.
 * The ImmutableStructure is a Structure implementation which is threadsafe, and
 * whose attributes can
 * not be modified after instantiation. All references are clones.
 */
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType"})
public final class ImmutableStructure extends AbstractStructure {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return "ImmutableStructure{" + "attributes=" + attributes + '}';
    }

    /**
     * create an immutable structure with the empty attributes.
     */
    public ImmutableStructure() {
        super();
    }

    /**
     * create immutable structure with the given attributes.
     *
     * @param attributes attributes.
     */
    public ImmutableStructure(Map<String, Value> attributes) {
        super(copyAttributes(attributes, null));
    }

    ImmutableStructure(String targetingKey, Map<String, Value> attributes) {
        super(copyAttributes(attributes, targetingKey));
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>(this.attributes.keySet());
    }

    // getters
    @Override
    public Value getValue(String key) {
        Value value = attributes.get(key);
        return value != null ? value.clone() : null;
    }

    /**
     * Get all values.
     *
     * @return all attributes on the structure
     */
    @Override
    public Map<String, Value> asMap() {
        return copyAttributes(attributes);
    }

    /**
     * Returns a builder for creating ImmutableStructure instances.
     *
     * @return a builder for ImmutableStructure
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder initialized with the current state of this object.
     *
     * @return a builder for ImmutableStructure
     */
    public Builder toBuilder() {
        return builder().attributes(this.asMap());
    }

    /**
     * Builder class for creating instances of ImmutableStructure.
     */
    public static class Builder {
        private final Map<String, Value> attributes;

        private Builder() {
            this.attributes = new HashMap<>();
        }

        /**
         * Sets the attributes from a map.
         *
         * @param attributes map of attributes
         * @return this builder
         */
        public Builder attributes(Map<String, Value> attributes) {
            if (attributes != null) {
                this.attributes.clear();
                this.attributes.putAll(attributes);
            }
            return this;
        }

        /**
         * Add String value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final String value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Integer value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final Integer value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Long value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final Long value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Float value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final Float value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Double value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final Double value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Boolean value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final Boolean value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Structure value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final Structure value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Value to the structure.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        public Builder add(final String key, final Value value) {
            attributes.put(key, value);
            return this;
        }

        /**
         * Build the ImmutableStructure with the provided values.
         *
         * @return a new ImmutableStructure instance
         */
        public ImmutableStructure build() {
            return new ImmutableStructure(new HashMap<>(attributes));
        }
    }

    private static Map<String, Value> copyAttributes(Map<String, Value> in) {
        return copyAttributes(in, null);
    }

    private static Map<String, Value> copyAttributes(Map<String, Value> in, String targetingKey) {
        Map<String, Value> copy = new HashMap<>();
        if (in != null) {
            for (Entry<String, Value> entry : in.entrySet()) {
                copy.put(
                        entry.getKey(),
                        Optional.ofNullable(entry.getValue())
                                .map((Value val) -> val.clone())
                                .orElse(null));
            }
        }
        if (targetingKey != null) {
            copy.put(EvaluationContext.TARGETING_KEY, new Value(targetingKey));
        }
        return copy;
    }
}

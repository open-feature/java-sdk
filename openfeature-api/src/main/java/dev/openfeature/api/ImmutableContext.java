package dev.openfeature.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 * The ImmutableContext is an EvaluationContext implementation which is
 * threadsafe, and whose attributes can
 * not be modified after instantiation.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
final class ImmutableContext implements EvaluationContext {

    private final ImmutableStructure structure;

    /**
     * Create an immutable context with an empty targeting_key and attributes
     * provided.
     */
    ImmutableContext() {
        this(new HashMap<>());
    }

    /**
     * Create an immutable context with given targeting_key provided.
     *
     * @param targetingKey targeting key
     */
    ImmutableContext(String targetingKey) {
        this(targetingKey, new HashMap<>());
    }

    /**
     * Create an immutable context with an attributes provided.
     *
     * @param attributes evaluation context attributes
     */
    ImmutableContext(Map<String, Value> attributes) {
        this(null, attributes);
    }

    /**
     * Create an immutable context with given targetingKey and attributes provided.
     *
     * @param targetingKey targeting key
     * @param attributes   evaluation context attributes
     */
    ImmutableContext(String targetingKey, Map<String, Value> attributes) {
        if (targetingKey != null && !targetingKey.trim().isEmpty()) {
            this.structure = new ImmutableStructure(targetingKey, attributes);
        } else {
            this.structure = new ImmutableStructure(attributes);
        }
    }

    /**
     * Retrieve targetingKey from the context.
     */
    @Override
    public String getTargetingKey() {
        Value value = this.getValue(TARGETING_KEY);
        return value == null ? null : value.asString();
    }

    // Delegated methods from ImmutableStructure
    @Override
    public boolean isEmpty() {
        return structure.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return structure.keySet();
    }

    @Override
    public Value getValue(String key) {
        return structure.getValue(key);
    }

    @Override
    public Map<String, Value> asMap() {
        return structure.asMap();
    }

    @Override
    public Map<String, Value> asUnmodifiableMap() {
        return structure.asUnmodifiableMap();
    }

    @Override
    public Map<String, Object> asObjectMap() {
        return structure.asObjectMap();
    }

    /**
     * Merges this EvaluationContext object with the passed EvaluationContext,
     * overriding in case of conflict.
     *
     * @param overridingContext overriding context
     * @return new, resulting merged context
     */
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        if (overridingContext == null || overridingContext.isEmpty()) {
            return new ImmutableContext(this.asUnmodifiableMap());
        }
        if (this.isEmpty()) {
            return new ImmutableContext(overridingContext.asUnmodifiableMap());
        }

        Map<String, Value> attributes = this.asMap();
        EvaluationContext.mergeMaps(ImmutableStructure::new, attributes, overridingContext.asUnmodifiableMap());
        return new ImmutableContext(attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImmutableContext that = (ImmutableContext) obj;
        return Objects.equals(structure, that.structure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure);
    }

    @Override
    public String toString() {
        return "ImmutableContext{" + "structure=" + structure + '}';
    }

    /**
     * Returns a builder initialized with the current state of this object.
     *
     * @return a builder for ImmutableContext
     */
    public ImmutableContextBuilder toBuilder() {
        return new Builder().targetingKey(this.getTargetingKey()).attributes(this.structure.asMap());
    }

    /**
     * Builder class for creating instances of ImmutableContext.
     */
    static class Builder implements ImmutableContextBuilder {
        private String targetingKey;
        private final Map<String, Value> attributes;

        Builder() {
            this.attributes = new HashMap<>();
        }

        /**
         * Sets the targeting key for the evaluation context.
         *
         * @param targetingKey the targeting key
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder targetingKey(String targetingKey) {
            this.targetingKey = targetingKey;
            return this;
        }

        /**
         * Sets the attributes from a map.
         *
         * @param attributes map of attributes
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder attributes(Map<String, Value> attributes) {
            if (attributes != null) {
                this.attributes.clear();
                this.attributes.putAll(attributes);
            }
            return this;
        }

        /**
         * Add String value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final String value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Integer value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final Integer value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Long value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final Long value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Float value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final Float value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Double value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final Double value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Boolean value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final Boolean value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Structure value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final Structure value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Value to the evaluation context.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public ImmutableContextBuilder add(final String key, final Value value) {
            attributes.put(key, value);
            return this;
        }

        /**
         * Build the ImmutableContext with the provided values.
         *
         * @return a new ImmutableContext instance
         */
        @Override
        public ImmutableContext build() {
            return new ImmutableContext(targetingKey, new HashMap<>(attributes));
        }
    }
}

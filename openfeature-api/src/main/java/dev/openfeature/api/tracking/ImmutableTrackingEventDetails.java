package dev.openfeature.api.tracking;

import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;
import dev.openfeature.api.types.ImmutableStructure;
import dev.openfeature.api.types.Structure;
import dev.openfeature.api.types.Value;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * ImmutableTrackingEventDetails represents data pertinent to a particular tracking event.
 */
class ImmutableTrackingEventDetails implements TrackingEventDetails {

    private final ImmutableStructure structure;
    private final Number value;

    public ImmutableTrackingEventDetails() {
        this.value = null;
        this.structure = new ImmutableStructure();
    }

    public ImmutableTrackingEventDetails(final Number value) {
        this.value = value;
        this.structure = new ImmutableStructure();
    }

    public ImmutableTrackingEventDetails(final Number value, final Map<String, Value> attributes) {
        this.value = value;
        this.structure = new ImmutableStructure(attributes);
    }

    /**
     * Returns the optional tracking value.
     */
    public Optional<Number> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public Value getValue(String key) {
        return structure.getValue(key);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImmutableTrackingEventDetails that = (ImmutableTrackingEventDetails) obj;
        return Objects.equals(structure, that.structure) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure, value);
    }

    @Override
    public String toString() {
        return "ImmutableTrackingEventDetails{" + "structure=" + structure + ", value=" + value + '}';
    }

    /**
     * Returns a builder initialized with the current state of this object.
     *
     * @return a builder for ImmutableTrackingEventDetails
     */
    public Builder toBuilder() {
        return new Builder().value(this.value).attributes(this.structure.asMap());
    }

    /**
     * Builder class for creating instances of ImmutableTrackingEventDetails.
     */
    public static class Builder implements ImmutableTrackingEventDetailsBuilder {
        private Number value;
        private final Map<String, Value> attributes;

        Builder() {
            this.attributes = new HashMap<>();
        }

        /**
         * Sets the numeric tracking value.
         *
         * @param value the tracking value
         * @return this builder
         */
        @Override
        public Builder value(Number value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the attributes from a map.
         *
         * @param attributes map of attributes
         * @return this builder
         */
        @Override
        public Builder attributes(Map<String, Value> attributes) {
            if (attributes != null) {
                this.attributes.clear();
                this.attributes.putAll(attributes);
            }
            return this;
        }

        /**
         * Add String value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final String value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Integer value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final Integer value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Long value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final Long value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Float value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final Float value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Double value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final Double value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Boolean value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final Boolean value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Structure value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final Structure value) {
            attributes.put(key, Value.objectToValue(value));
            return this;
        }

        /**
         * Add Value to the tracking event details.
         *
         * @param key   attribute key
         * @param value attribute value
         * @return this builder
         */
        @Override
        public Builder add(final String key, final Value value) {
            attributes.put(key, value);
            return this;
        }

        /**
         * Build the ImmutableTrackingEventDetails with the provided values.
         *
         * @return a new ImmutableTrackingEventDetails instance
         */
        @Override
        public TrackingEventDetails build() {
            return new ImmutableTrackingEventDetails(value, new HashMap<>(attributes));
        }
    }

    @SuppressWarnings("all")
    private static class DelegateExclusions {
        @ExcludeFromGeneratedCoverageReport
        public <T extends Structure> Map<String, Value> merge(
                Function<Map<String, Value>, Structure> newStructure,
                Map<String, Value> base,
                Map<String, Value> overriding) {
            return null;
        }
    }
}

package dev.openfeature.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an evaluation event.
 * This class is immutable and thread-safe.
 */
public class EvaluationEvent {

    private final String name;
    private final Map<String, Object> attributes;

    /**
     * Private constructor - use builder() to create instances.
     */
    private EvaluationEvent(String name, Map<String, Object> attributes) {
        this.name = name;
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    /**
     * Gets the name of the evaluation event.
     *
     * @return the event name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a copy of the event attributes.
     *
     * @return a new map containing the event attributes
     */
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EvaluationEvent that = (EvaluationEvent) obj;
        return Objects.equals(name, that.name) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, attributes);
    }

    @Override
    public String toString() {
        return "EvaluationEvent{" + "name='" + name + '\'' + ", attributes=" + attributes + '}';
    }

    /**
     * Builder class for creating instances of EvaluationEvent.
     */
    public static class Builder {
        private String name;
        private Map<String, Object> attributes = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public EvaluationEvent build() {
            return new EvaluationEvent(name, attributes);
        }
    }
}

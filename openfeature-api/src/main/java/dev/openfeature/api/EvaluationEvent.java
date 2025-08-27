package dev.openfeature.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an evaluation event.
 */
public class EvaluationEvent {

    private String name;
    private Map<String, Object> attributes;

    public EvaluationEvent() {
        this.attributes = new HashMap<>();
    }

    public EvaluationEvent(String name, Map<String, Object> attributes) {
        this.name = name;
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    public static EvaluationEventBuilder builder() {
        return new EvaluationEventBuilder();
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
    public static class EvaluationEventBuilder {
        private String name;
        private Map<String, Object> attributes = new HashMap<>();

        public EvaluationEventBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EvaluationEventBuilder attributes(Map<String, Object> attributes) {
            this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
            return this;
        }

        public EvaluationEventBuilder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public EvaluationEvent build() {
            return new EvaluationEvent(name, attributes);
        }
    }
}

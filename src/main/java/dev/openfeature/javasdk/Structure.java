package dev.openfeature.javasdk;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

// Note: We don't accept instances of T for EC b/c walking arbitrary objects to serialize them isn't quite a straight
// forward for providers. They may not have access to tools like Jackson or Gson.

/**
 * {@link Structure} represents a potentially nested object type which is used to pass complex objects via
 * {@link EvaluationContext}.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class Structure {

    protected final Map<String, Value> attributes;

    public Structure() {
        this.attributes = new HashMap<>();
    }

    public Structure(Map<String, Value> attributes) {
        this.attributes = new HashMap<>(attributes);
    }

    public Set<String> keySet() {
        return this.attributes.keySet();
    }

    // getters
    public Value getValue(String key) {
        return this.attributes.get(key);
    }

    // adders
    public Structure add(String key, Value value) {
        attributes.put(key, value);
        return this;
    }

    public Structure add(String key, Boolean value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public Structure add(String key, String value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public Structure add(String key, Integer value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public Structure add(String key, Double value) {
        attributes.put(key, new Value(value));
        return this;
    }

    /** 
     * Add date-time relevant key.
     * 
     * @param key feature key
     * @param value date-time value
     * @return Structure
     */
    public Structure add(String key, ZonedDateTime value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public Structure add(String key, Structure value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public <T> Structure add(String key, List<Value> value) {
        attributes.put(key, new Value(value));
        return this;
    }

    /**
     * Get all values.
     * 
     * @return all attributes on the structure
     */
    public Map<String, Value> asMap() {
        return new HashMap<>(this.attributes);
    }
}

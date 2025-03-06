package dev.openfeature.sdk;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.ToString;

/**
 * {@link MutableStructure} represents a potentially nested object type which is used to represent
 * structured data.
 * The MutableStructure is a Structure implementation which is not threadsafe, and whose attributes can
 * be modified after instantiation.
 */
@ToString
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType"})
public class MutableStructure extends AbstractStructure {

    public MutableStructure() {
        super();
    }

    public MutableStructure(Map<String, Value> attributes) {
        super(attributes);
    }

    @Override
    public Set<String> keySet() {
        return attributes.keySet();
    }

    // getters
    @Override
    public Value getValue(String key) {
        return attributes.get(key);
    }

    // adders
    public MutableStructure add(String key, Value value) {
        attributes.put(key, value);
        return this;
    }

    public MutableStructure add(String key, Boolean value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, String value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Integer value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Double value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Instant value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Structure value) {
        attributes.put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, List<Value> value) {
        attributes.put(key, new Value(value));
        return this;
    }

    /**
     * Get all values.
     *
     * @return all attributes on the structure
     */
    @Override
    public Map<String, Value> asMap() {
        return new HashMap<>(attributes);
    }
}

package dev.openfeature.sdk;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * {@link MutableStructure} represents a potentially nested object type which is used to represent 
 * structured data.
 * The MutableStructure is a Structure implementation which is not threadsafe, and whose attributes can 
 * be modified after instantiation.
 */
@ToString
@EqualsAndHashCode
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
        return getAttributes().keySet();
    }

    // getters
    @Override
    public Value getValue(String key) {
        return getAttributes().get(key);
    }

    // adders
    public MutableStructure add(String key, Value value) {
        getAttributes().put(key, value);
        return this;
    }

    public MutableStructure add(String key, Boolean value) {
        getAttributes().put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, String value) {
        getAttributes().put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Integer value) {
        getAttributes().put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Double value) {
        getAttributes().put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Instant value) {
        getAttributes().put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, Structure value) {
        getAttributes().put(key, new Value(value));
        return this;
    }

    public MutableStructure add(String key, List<Value> value) {
        getAttributes().put(key, new Value(value));
        return this;
    }

    /**
     * Get all values.
     *
     * @return all attributes on the structure
     */
    @Override
    public Map<String, Value> asMap() {
        return new HashMap<>(getAttributes());
    }
}

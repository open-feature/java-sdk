package dev.openfeature.sdk;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.openfeature.sdk.Value.objectToValue;

/**
 * {@link MutableStructure} represents a potentially nested object type which is used to represent 
 * structured data.
 * The MutableStructure is a Structure implementation which is not threadsafe, and whose attributes can 
 * be modified after instantiation.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType"})
public class MutableStructure implements Structure {

    protected final Map<String, Value> attributes;

    public MutableStructure() {
        this.attributes = new HashMap<>();
    }

    public MutableStructure(Map<String, Value> attributes) {
        this.attributes = new HashMap<>(attributes);
    }

    @Override
    public Set<String> keySet() {
        return this.attributes.keySet();
    }

    // getters
    @Override
    public Value getValue(String key) {
        return this.attributes.get(key);
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

    public <T> MutableStructure add(String key, List<Value> value) {
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
        return new HashMap<>(this.attributes);
    }

    /**
     * Get all values, with primitives types.
     *
     * @return all attributes on the structure into a Map
     */
    @Override
    public Map<String, Object> asObjectMap() {
        return attributes
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> convertValue(getValue(e.getKey()))
            ));
    }

    /**
     * Transform an object map to a {@link Structure} type.
     *
     * @param map map of objects
     * @return a Structure object in the SDK format
     */
    public static Structure mapToStructure(Map<String, Object> map) {
        return new MutableStructure(
            map.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> objectToValue(e.getValue()))));
    }
}

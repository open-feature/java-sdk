package dev.openfeature.sdk;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dev.openfeature.sdk.exceptions.ValueNotConvertableError;
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
     * convertValue is converting the object type Value in a primitive type.
     *
     * @param value - Value object to convert
     * @return an Object containing the primitive type.
     */
    private Object convertValue(Value value) {
        if (value.isBoolean()) {
            return value.asBoolean();
        }

        if (value.isNumber()) {
            Double valueAsDouble = value.asDouble();
            if (valueAsDouble == Math.floor(valueAsDouble) && !Double.isInfinite(valueAsDouble)) {
                return value.asInteger();
            }
            return valueAsDouble;
        }

        if (value.isString()) {
            return value.asString();
        }

        if (value.isInstant()) {
            return value.asInstant();
        }

        if (value.isList()) {
            return value.asList()
                    .stream()
                    .map(this::convertValue)
                    .collect(Collectors.toList());
        }

        if (value.isStructure()) {
            Structure s = value.asStructure();
            return s.asMap()
                    .keySet()
                    .stream()
                    .collect(
                        Collectors.toMap(
                            key -> key,
                            key -> convertValue(s.getValue(key))
                        )
                    );
        }
        throw new ValueNotConvertableError();
    }
}

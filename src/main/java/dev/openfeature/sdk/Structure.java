package dev.openfeature.sdk;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import dev.openfeature.sdk.exceptions.ValueNotConvertableError;
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
    public Structure add(String key, Instant value) {
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

    /**
     * Get all values, with primitives types.
     *
     * @return all attributes on the structure into a Map
     */
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

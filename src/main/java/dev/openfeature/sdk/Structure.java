package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.ValueNotConvertableError;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.openfeature.sdk.Value.objectToValue;

/**
 * {@link Structure} represents a potentially nested object type which is used to represent 
 * structured data.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public interface Structure {
    
    /**
     * Boolean indicating if this structure is empty.
     * @return boolean for emptiness
     */
    boolean isEmpty();

    /**
     * Get all keys.
     *
     * @return the set of keys
     */
    Set<String> keySet();

    /**
     * Get the value indexed by key.
     *
     * @param key String the key.
     * @return the Value
     */
    Value getValue(String key);

    /**
     * Get all values, as a map of Values.
     *
     * @return all attributes on the structure into a Map
     */
    Map<String, Value> asMap();

    /**
     * Get all values, as a map of Values.
     *
     * @return all attributes on the structure into a Map
     */
    Map<String, Value> asUnmodifiableMap();


    /**
     * Get all values, with as a map of Object.
     *
     * @return all attributes on the structure into a Map
     */
    Map<String, Object> asObjectMap();

    /**
     * Converts the Value into its equivalent primitive type.
     *
     * @param value - Value object to convert
     * @return an Object containing the primitive type, or null.
     */
    default Object convertValue(Value value) {

        if (value == null || value.isNull()) {
            return null;
        }

        if (value.isBoolean()) {
            return value.asBoolean();
        }

        if (value.isNumber() && !value.isNull()) {
            Number numberValue = (Number) value.asObject();
            if (numberValue instanceof Double) {
                return numberValue.doubleValue();
            } else if (numberValue instanceof Integer) {
                return numberValue.intValue();
            }
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
            return s.asUnmodifiableMap()
                    .entrySet()
                    .stream()
                    .collect(HashMap::new,
                            (accumulated, entry) -> accumulated.put(entry.getKey(),
                                    convertValue(entry.getValue())),
                            HashMap::putAll);
        }

        throw new ValueNotConvertableError();
    }

    /**
     * Transform an object map to a {@link Structure} type.
     *
     * @param map map of objects
     * @return a Structure object in the SDK format
     */
    static Structure mapToStructure(Map<String, Object> map) {
        return new MutableStructure(map.entrySet().stream()
            .collect(HashMap::new,
                        (accumulated, entry) -> accumulated.put(entry.getKey(),
                        objectToValue(entry.getValue())),
                        HashMap::putAll));
    }
}

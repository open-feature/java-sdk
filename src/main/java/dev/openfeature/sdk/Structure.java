package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.ValueNotConvertableError;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Structure} represents a potentially nested object type which is used to represent 
 * structured data.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public interface Structure {
    
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
     * Get all values, with as a map of Object.
     *
     * @return all attributes on the structure into a Map
     */
    Map<String, Object> asObjectMap();

    /**
     * convertValue is converting the object type Value in a primitive type.
     *
     * @param value - Value object to convert
     * @return an Object containing the primitive type.
     */
    default Object convertValue(Value value) {
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

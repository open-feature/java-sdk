package dev.openfeature.sdk;

import java.util.Map;
import java.util.Set;

/**
 * {@link Structure} represents a potentially nested object type which is used to pass complex objects via
 * {@link EvaluationContext}.
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
}

package dev.openfeature.sdk;

import java.util.Map;
import java.util.Set;

/**
 * {@link Structure} represents a potentially nested object type which is used to pass complex objects via
 * {@link EvaluationContext}.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public interface Structure {
    
    Set<String> keySet();

    Value getValue(String key);

    Map<String, Value> asMap();

    /**
     * Get all values, with primitives types.
     *
     * @return all attributes on the structure into a Map
     */
    Map<String, Object> asObjectMap();
}

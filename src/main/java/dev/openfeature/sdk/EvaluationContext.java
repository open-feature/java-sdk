package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public interface EvaluationContext extends Structure {

    String TARGETING_KEY = "targetingKey";

    String getTargetingKey();

    /**
     * Merges this EvaluationContext object with the second overriding the this in
     * case of conflict.
     *
     * @param overridingContext overriding context
     * @return resulting merged context
     */
    EvaluationContext merge(EvaluationContext overridingContext);

    /**
     * Recursively merges the base Value map with the overriding map.
     * 
     * @param <T> Structure type
     * @param newStructure function to create the right structure(s) for Values
     * @param base base map to merge
     * @param overriding overriding map to merge
     * @return resulting merged map
     */
    static <T extends Structure> Map<String, Value> mergeMaps(Function<Map<String, Value>, Structure> newStructure,
            Map<String, Value> base,
            Map<String, Value> overriding) {

        if (base == null || base.isEmpty()) {
            return overriding;
        }
        if (overriding == null || overriding.isEmpty()) {
            return base;
        }

        final Map<String, Value> merged = new HashMap<>(base);
        for (Entry<String, Value> overridingEntry : overriding.entrySet()) {
            String key = overridingEntry.getKey();
            if (overridingEntry.getValue().isStructure() && merged.containsKey(key) && merged.get(key).isStructure()) {
                Structure mergedValue = merged.get(key).asStructure();
                Structure overridingValue = overridingEntry.getValue().asStructure();
                Map<String, Value> newMap = mergeMaps(newStructure, mergedValue.asUnmodifiableMap(),
                        overridingValue.asUnmodifiableMap());
                merged.put(key, new Value(newStructure.apply(newMap)));
            } else {
                merged.put(key, overridingEntry.getValue());
            }
        }
        return merged;
    }
}

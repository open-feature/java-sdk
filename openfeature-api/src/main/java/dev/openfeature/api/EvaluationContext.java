package dev.openfeature.api;

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

    /**
     * Empty evaluation context for use as a default.
     */
    EvaluationContext EMPTY = new ImmutableContext();

    static EvaluationContext immutableOf(Map<String, Value> attributes) {
        return new ImmutableContext(attributes);
    }

    static EvaluationContext immutableOf(String targetingKey, Map<String, Value> attributes) {
        return new ImmutableContext(targetingKey, attributes);
    }

    static ImmutableContextBuilder immutableBuilder() {
        return new ImmutableContext.Builder();
    }

    static ImmutableContextBuilder immutableBuilder(EvaluationContext original) {
        return new ImmutableContext.Builder().attributes(original.asMap()).targetingKey(original.getTargetingKey());
    }

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
     * Recursively merges the overriding map into the base Value map.
     * The base map is mutated, the overriding map is not.
     * Null maps will cause no-op.
     *
     * @param newStructure function to create the right structure(s) for Values
     * @param base         base map to merge
     * @param overriding   overriding map to merge
     */
    static void mergeMaps(
            Function<Map<String, Value>, Structure> newStructure,
            Map<String, Value> base,
            Map<String, Value> overriding) {

        if (base == null) {
            return;
        }
        if (overriding == null || overriding.isEmpty()) {
            return;
        }

        for (Entry<String, Value> overridingEntry : overriding.entrySet()) {
            String key = overridingEntry.getKey();
            if (overridingEntry.getValue().isStructure()
                    && base.containsKey(key)
                    && base.get(key).isStructure()) {
                Structure mergedValue = base.get(key).asStructure();
                Structure overridingValue = overridingEntry.getValue().asStructure();
                Map<String, Value> newMap = mergedValue.asMap();
                mergeMaps(newStructure, newMap, overridingValue.asUnmodifiableMap());
                base.put(key, new Value(newStructure.apply(newMap)));
            } else {
                base.put(key, overridingEntry.getValue());
            }
        }
    }
}

package dev.openfeature.api.evaluation;

import dev.openfeature.api.types.MutableStructure;
import dev.openfeature.api.types.Structure;
import dev.openfeature.api.types.Value;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 * The MutableContext is an EvaluationContext implementation which is not threadsafe, and whose attributes can
 * be modified after instantiation.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class MutableContext implements EvaluationContext {

    private final MutableStructure structure;

    public MutableContext() {
        this(new HashMap<>());
    }

    public MutableContext(String targetingKey) {
        this(targetingKey, new HashMap<>());
    }

    public MutableContext(Map<String, Value> attributes) {
        this(null, new HashMap<>(attributes));
    }

    /**
     * Create a mutable context with given targetingKey and attributes provided. TargetingKey should be non-null
     * and non-empty to be accepted.
     *
     * @param targetingKey targeting key
     * @param attributes   evaluation context attributes
     */
    public MutableContext(String targetingKey, Map<String, Value> attributes) {
        this.structure = new MutableStructure(new HashMap<>(attributes));
        if (targetingKey != null && !targetingKey.trim().isEmpty()) {
            this.structure.add(TARGETING_KEY, targetingKey);
        }
    }

    // override @Delegate methods so that we can use "add" methods and still return MutableContext, not Structure
    public MutableContext add(String key, Boolean value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, String value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, Integer value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, Double value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, Instant value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, Structure value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, List<Value> value) {
        this.structure.add(key, value);
        return this;
    }

    /**
     * Override or set targeting key for this mutable context. Value should be non-null and non-empty to be accepted.
     */
    public MutableContext setTargetingKey(String targetingKey) {
        if (targetingKey != null && !targetingKey.trim().isEmpty()) {
            this.add(TARGETING_KEY, targetingKey);
        }
        return this;
    }

    // Delegated methods from MutableStructure
    @Override
    public boolean isEmpty() {
        return structure.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return structure.keySet();
    }

    @Override
    public Value getValue(String key) {
        return structure.getValue(key);
    }

    @Override
    public Map<String, Value> asMap() {
        return structure.asMap();
    }

    @Override
    public Map<String, Value> asUnmodifiableMap() {
        return structure.asUnmodifiableMap();
    }

    @Override
    public Map<String, Object> asObjectMap() {
        return structure.asObjectMap();
    }

    /**
     * Retrieve targetingKey from the context.
     */
    @Override
    public String getTargetingKey() {
        Value value = this.getValue(TARGETING_KEY);
        return value == null ? null : value.asString();
    }

    /**
     * Merges this EvaluationContext objects with the second overriding the in case of conflict.
     *
     * @param overridingContext overriding context
     * @return resulting merged context
     */
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        if (overridingContext == null || overridingContext.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return overridingContext;
        }

        Map<String, Value> attributes = this.asMap();
        EvaluationContext.mergeMaps(MutableStructure::new, attributes, overridingContext.asUnmodifiableMap());
        return new MutableContext(attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MutableContext that = (MutableContext) obj;
        return Objects.equals(structure, that.structure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure);
    }

    @Override
    public String toString() {
        return "MutableContext{" + "structure=" + structure + '}';
    }
}

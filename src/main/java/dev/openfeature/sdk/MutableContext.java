package dev.openfeature.sdk;

import dev.openfeature.sdk.internal.ExcludeFromGeneratedCoverageReport;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 * The MutableContext is an EvaluationContext implementation which is not threadsafe, and whose attributes can
 * be modified after instantiation.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class MutableContext implements EvaluationContext {

    @Delegate(excludes = DelegateExclusions.class)
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
            this.structure.attributes.put(TARGETING_KEY, new Value(targetingKey));
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

    /**
     * Hidden class to tell Lombok not to copy these methods over via delegation.
     */
    @SuppressWarnings("all")
    private static class DelegateExclusions {

        @ExcludeFromGeneratedCoverageReport
        public <T extends Structure> Map<String, Value> merge(
                Function<Map<String, Value>, Structure> newStructure,
                Map<String, Value> base,
                Map<String, Value> overriding) {

            return null;
        }

        public MutableStructure add(String ignoredKey, Boolean ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, Double ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, String ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, Value ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, Integer ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, List<Value> ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, Structure ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, Instant ignoredValue) {
            return null;
        }
    }
}

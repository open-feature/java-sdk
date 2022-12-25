package dev.openfeature.sdk;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 * The MutableContext is an EvaluationContext implementation which is not threadsafe, and whose attributes can
 * be modified after instantiation.
 */
@ToString
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class ImmutableContext implements EvaluationContext {

    @Getter
    private String targetingKey;
    @Delegate(excludes = HideDelegateAddMethods.class)
    private final MutableStructure structure;

    public ImmutableContext() {
        this.structure = new MutableStructure();
        this.targetingKey = "";
    }

    public ImmutableContext(Map<String, Value> attributes) {
        HashMap<String, Value> copy = new HashMap<>(attributes);
        this.structure = new MutableStructure(copy);
        this.targetingKey = "";
    }

    public ImmutableContext(String targetingKey, Map<String, Value> attributes) {
        this(attributes);
        this.targetingKey = targetingKey;
    }

    @Override
    public void setTargetingKey(String targetingKey) {
        throw new UnsupportedOperationException("changing of targeting key is not allowed");
    }

    /**
     * Merges this EvaluationContext objects with the second overriding the this in
     * case of conflict.
     *
     * @param overridingContext overriding context
     * @return resulting merged context
     */
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        if (overridingContext == null) {
            return new ImmutableContext(this.asMap());
        }
        String targetingKey = "";
        if (this.getTargetingKey() != null && !this.getTargetingKey().trim().equals("")) {
            targetingKey = this.getTargetingKey();
        }

        if (overridingContext.getTargetingKey() != null && !overridingContext.getTargetingKey().trim().equals("")) {
            targetingKey = overridingContext.getTargetingKey();
        }
        Map<String, Value> merged = new HashMap<>();

        merged.putAll(this.asMap());
        merged.putAll(overridingContext.asMap());
        return new ImmutableContext(targetingKey, merged);
    }

    /**
     * Hidden class to tell Lombok not to copy these methods over via delegation.
     */
    private static class HideDelegateAddMethods {
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

        public MutableStructure add(String ignoredKey, MutableStructure ignoredValue) {
            return null;
        }

        public MutableStructure add(String ignoredKey, Instant ignoredValue) {
            return null;
        }
    }
}

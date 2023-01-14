package dev.openfeature.sdk;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 * The ImmutableContext is an EvaluationContext implementation which is threadsafe, and whose attributes can
 * not be modified after instantiation.
 */
@ToString
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public final class ImmutableContext implements EvaluationContext {

    @Getter
    private final String targetingKey;
    @Delegate
    private final Structure structure;

    /**
     * create an immutable context with an empty targeting_key and attributes provided.
     */
    public ImmutableContext() {
        this("", new HashMap<>());
    }

    /**
     * create an immutable context with an attributes provided.
     *
     * @param attributes evaluation context attributes
     */
    public ImmutableContext(Map<String, Value> attributes) {
        this("", attributes);
    }

    /**
     * create an immutable context with given targetingKey and attributes provided.
     *
     * @param targetingKey targeting key
     * @param attributes   evaluation context attributes
     */
    public ImmutableContext(String targetingKey, Map<String, Value> attributes) {
        this.structure = new ImmutableStructure(attributes);
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
}

package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;

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
     * Create an immutable context with an empty targeting_key and attributes provided.
     */
    public ImmutableContext() {
        this("", new HashMap<>());
    }

    /**
     * Create an immutable context with given targeting_key provided.
     *
     * @param targetingKey targeting key
     */
    public ImmutableContext(String targetingKey) {
        this(targetingKey, new HashMap<>());
    }

    /**
     * Create an immutable context with an attributes provided.
     *
     * @param attributes evaluation context attributes
     */
    public ImmutableContext(Map<String, Value> attributes) {
        this("", attributes);
    }

    /**
     * Create an immutable context with given targetingKey and attributes provided.
     *
     * @param targetingKey targeting key
     * @param attributes   evaluation context attributes
     */
    public ImmutableContext(String targetingKey, Map<String, Value> attributes) {
        this.structure = new ImmutableStructure(attributes);
        this.targetingKey = targetingKey;
    }

    /**
     * Mutating targeting key is not supported in ImmutableContext and will be removed.
     */
    @Override
    @Deprecated
    public void setTargetingKey(String targetingKey) {
        throw new UnsupportedOperationException("changing of targeting key is not allowed");
    }

    /**
     * Merges this EvaluationContext object with the passed EvaluationContext, overriding in case of conflict.
     *
     * @param overridingContext overriding context
     * @return resulting merged context
     */
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        if (overridingContext == null) {
            return new ImmutableContext(this.targetingKey, this.asMap());
        }
        String newTargetingKey = "";
        if (this.getTargetingKey() != null && !this.getTargetingKey().trim().equals("")) {
            newTargetingKey = this.getTargetingKey();
        }

        if (overridingContext.getTargetingKey() != null && !overridingContext.getTargetingKey().trim().equals("")) {
            newTargetingKey = overridingContext.getTargetingKey();
        }
        Map<String, Value> merged = new HashMap<>();

        merged.putAll(this.asMap());
        merged.putAll(overridingContext.asMap());
        return new ImmutableContext(newTargetingKey, merged);
    }
}

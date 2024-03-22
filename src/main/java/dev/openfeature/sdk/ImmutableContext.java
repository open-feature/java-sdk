package dev.openfeature.sdk;

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

    @Delegate
    private final Structure structure;

    /**
     * Create an immutable context with an empty targeting_key and attributes provided.
     */
    public ImmutableContext() {
        this(new HashMap<>());
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
        if (targetingKey != null && !targetingKey.trim().isEmpty()) {
            attributes.put(TARGETING_KEY, new Value(targetingKey));
        }
        this.structure = new ImmutableStructure(attributes);
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
     * Merges this EvaluationContext object with the passed EvaluationContext, overriding in case of conflict.
     *
     * @param overridingContext overriding context
     * @return resulting merged context
     */
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        if (overridingContext == null) {
            return new ImmutableContext(this.asMap());
        }

        return new ImmutableContext(
                this.merge(ImmutableStructure::new, this.asMap(), overridingContext.asMap()));
    }
}

package dev.openfeature.sdk;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public interface EvaluationContext extends Structure {
    String getTargetingKey();
    
    void setTargetingKey(String targetingKey);

    /**
     * Merges this EvaluationContext object with the second overriding the this in
     * case of conflict.
     *
     * @param overridingContext overriding context
     * @return resulting merged context
     */
    EvaluationContext merge(EvaluationContext overridingContext);
}

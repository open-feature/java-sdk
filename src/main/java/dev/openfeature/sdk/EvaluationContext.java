package dev.openfeature.sdk;

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

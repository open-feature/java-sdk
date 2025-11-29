package dev.openfeature.sdk;

import dev.openfeature.sdk.internal.ExcludeFromGeneratedCoverageReport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 * The ImmutableContext is an EvaluationContext implementation which is
 * threadsafe, and whose attributes can
 * not be modified after instantiation.
 */
@ToString
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public final class ImmutableContext implements EvaluationContext {

    public static final ImmutableContext EMPTY = new ImmutableContext(Collections.emptyMap());

    @Delegate(excludes = DelegateExclusions.class)
    private final ImmutableStructure structure;

    // Lazily computed hash code, safe because this class is immutable.
    private volatile Integer cachedHashCode;

    /**
     * Create an immutable context with an empty targeting_key and attributes
     * provided.
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
        this(null, attributes);
    }

    /**
     * Create an immutable context with given targetingKey and attributes provided.
     *
     * @param targetingKey targeting key
     * @param attributes   evaluation context attributes
     */
    public ImmutableContext(String targetingKey, Map<String, Value> attributes) {
        if (targetingKey != null && !targetingKey.isBlank()) {
            this.structure = new ImmutableStructure(targetingKey, attributes);
        } else {
            this.structure = new ImmutableStructure(attributes);
        }
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
     * Merges this EvaluationContext object with the passed EvaluationContext,
     * overriding in case of conflict.
     *
     * @param overridingContext overriding context
     * @return new, resulting merged context
     */
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        if (overridingContext == null || overridingContext.isEmpty()) {
            return new ImmutableContext(this.asUnmodifiableMap());
        }
        if (this.isEmpty()) {
            return new ImmutableContext(overridingContext.asUnmodifiableMap());
        }

        Map<String, Value> attributes = this.asMap();
        EvaluationContext.mergeMaps(ImmutableStructure::new, attributes, overridingContext.asUnmodifiableMap());
        return new ImmutableContext(attributes);
    }

    /**
     * Equality for EvaluationContext implementations is defined in terms of their resolved
     * attribute maps. Two contexts are considered equal if their {@link #asMap()} representations
     * contain the same key/value pairs, regardless of how the context was constructed or layered.
     *
     * @param o the object to compare with this context
     * @return true if the other object is an EvaluationContext whose resolved attributes match
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EvaluationContext)) {
            return false;
        }
        EvaluationContext that = (EvaluationContext) o;
        return this.asMap().equals(that.asMap());
    }

    /**
     * Computes a hash code consistent with {@link #equals(Object)}. Since this context is immutable,
     * the hash code is lazily computed once from its resolved attribute map and then cached.
     *
     * @return the cached hash code derived from this context's attribute map
     */
    @Override
    public int hashCode() {
        Integer result = cachedHashCode;
        if (result == null) {
            synchronized (this) {
                result = cachedHashCode;
                if (result == null) {
                    result = asMap().hashCode();
                    cachedHashCode = result;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("all")
    private static class DelegateExclusions {
        @ExcludeFromGeneratedCoverageReport
        public <T extends Structure> Map<String, Value> merge(
                Function<Map<String, Value>, Structure> newStructure,
                Map<String, Value> base,
                Map<String, Value> overriding) {
            return null;
        }
    }
}

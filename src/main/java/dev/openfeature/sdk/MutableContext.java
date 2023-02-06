package dev.openfeature.sdk;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * The EvaluationContext is a container for arbitrary contextual data
 * that can be used as a basis for dynamic evaluation.
 * The MutableContext is an EvaluationContext implementation which is not threadsafe, and whose attributes can 
 * be modified after instantiation.
 */
@ToString
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class MutableContext implements EvaluationContext {

    @Setter() @Getter private String targetingKey;
    @Delegate(excludes = HideDelegateAddMethods.class) private final MutableStructure structure;

    public MutableContext() {
        this.structure = new MutableStructure();
        this.targetingKey = "";
    }

    public MutableContext(String targetingKey) {
        this();
        this.targetingKey = targetingKey;
    }

    public MutableContext(Map<String, Value> attributes) {
        this.structure = new MutableStructure(attributes);
        this.targetingKey = "";
    }

    public MutableContext(String targetingKey, Map<String, Value> attributes) {
        this(attributes);
        this.targetingKey = targetingKey;
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
     * Merges this EvaluationContext objects with the second overriding the this in
     * case of conflict.
     *
     * @param overridingContext overriding context
     * @return resulting merged context
     */
    @Override
    public EvaluationContext merge(EvaluationContext overridingContext) {
        if (overridingContext == null) {
            return new MutableContext(this.asMap());
        }

        Map<String, Value> merged = this.merge(this.asMap(), overridingContext.asMap());

        String newTargetingKey = "";

        if (this.getTargetingKey() != null && !this.getTargetingKey().trim().equals("")) {
            newTargetingKey = this.getTargetingKey();
        }

        if (overridingContext.getTargetingKey() != null && !overridingContext.getTargetingKey().trim().equals("")) {
            newTargetingKey = overridingContext.getTargetingKey();
        }
        
        EvaluationContext ec = null;
        if (newTargetingKey != null && !newTargetingKey.trim().equals("")) {
            ec = new MutableContext(newTargetingKey, merged);
        } else {
            ec = new MutableContext(merged);
        }

        return ec;
    }
    

    /**
     * Recursively merges the base map from this EvaluationContext with the passed EvaluationContext.
     * 
     * @param base base map to merge
     * @param overriding overriding map to merge
     * @return resulting merged map
     */
    private Map<String, Value> merge(Map<String, Value> base, Map<String, Value> overriding) {
        Map<String, Value> merged = new HashMap<>();

        merged.putAll(base);
        for (Entry<String, Value> overridingEntry : overriding.entrySet()) {
            String key = overridingEntry.getKey();
            if (overridingEntry.getValue().isStructure() && merged.containsKey(key) && merged.get(key).isStructure()) {
                Structure mergedValue = merged.get(key).asStructure();
                Structure overridingValue = overridingEntry.getValue().asStructure();
                Map<String, Value> newMap = this.merge(mergedValue.asMap(), overridingValue.asMap());
                merged.put(key, new Value(new MutableContext(newMap)));
            } else {
                merged.put(key, overridingEntry.getValue());
            }
        }
        return merged;
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

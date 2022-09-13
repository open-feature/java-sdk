package dev.openfeature.javasdk;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class EvaluationContext {

    @Setter @Getter private String targetingKey;
    @Delegate(excludes = HideDelegateAddMethods.class) private final Structure structure = new Structure();

    public EvaluationContext() {
        super();
        this.targetingKey = "";
    }

    public EvaluationContext(String targetingKey) {
        this();
        this.targetingKey = targetingKey;
    }

    /**
     * Merges two EvaluationContext objects with the second overriding the first in
     * case of conflict.
     *
     * @param ctx1 base context
     * @param ctx2 overriding context
     * @return resulting merged context
     */
    public static EvaluationContext merge(EvaluationContext ctx1, EvaluationContext ctx2) {
        EvaluationContext ec = new EvaluationContext();
        if (ctx1 == null) {
            return ctx2;
        } else if (ctx2 == null) {
            return ctx1;
        }

        ec.structure.attributes.putAll(ctx1.structure.attributes);
        ec.structure.attributes.putAll(ctx2.structure.attributes);

        if (ctx1.getTargetingKey() != null && !ctx1.getTargetingKey().trim().equals("")) {
            ec.setTargetingKey(ctx1.getTargetingKey());
        }

        if (ctx2.getTargetingKey() != null && !ctx2.getTargetingKey().trim().equals("")) {
            ec.setTargetingKey(ctx2.getTargetingKey());
        }

        return ec;
    }

    // override @Delegate methods so that we can use "add" methods and still return EvaluationContext, not Structure
    public EvaluationContext add(String key, Boolean value) {
        this.structure.add(key, value);
        return this;
    }

    public EvaluationContext add(String key, String value) {
        this.structure.add(key, value);
        return this;
    }

    public EvaluationContext add(String key, Integer value) {
        this.structure.add(key, value);
        return this;
    }

    public EvaluationContext add(String key, Double value) {
        this.structure.add(key, value);
        return this;
    }

    public EvaluationContext add(String key, Instant value) {
        this.structure.add(key, value);
        return this;
    }

    public EvaluationContext add(String key, Structure value) {
        this.structure.add(key, value);
        return this;
    }

    public EvaluationContext add(String key, List<Value> value) {
        this.structure.add(key, value);
        return this;
    }

    /**
     * Hidden class to tell Lombok not to copy these methods over via delegation.
     */
    private static class HideDelegateAddMethods {
        public Structure add(String ignoredKey, Boolean ignoredValue) {
            return null;
        }
        
        public Structure add(String ignoredKey, Double ignoredValue) {
            return null;
        }

        public Structure add(String ignoredKey, String ignoredValue) {
            return null;
        }

        public Structure add(String ignoredKey, Value ignoredValue) {
            return null;
        }

        public Structure add(String ignoredKey, Integer ignoredValue) {
            return null;
        }

        public Structure add(String ignoredKey, List<Value> ignoredValue) {
            return null;
        }

        public Structure add(String ignoredKey, Structure ignoredValue) {
            return null;
        }

        public Structure add(String ignoredKey, Instant ignoredValue) {
            return null;
        }
    }
}

package dev.openfeature.javasdk;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString @EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class EvaluationContext extends Structure {
    @Setter @Getter private String targetingKey;

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

        ec.attributes.putAll(ctx1.attributes);
        ec.attributes.putAll(ctx2.attributes);

        if (ctx1.getTargetingKey() != null && !ctx1.getTargetingKey().trim().equals("")) {
            ec.setTargetingKey(ctx1.getTargetingKey());
        }

        if (ctx2.getTargetingKey() != null && !ctx2.getTargetingKey().trim().equals("")) {
            ec.setTargetingKey(ctx2.getTargetingKey());
        }

        return ec;
    }

    // overrides so we can use "add" methods and still return EvaluationContext, not superclass
    @Override
    public EvaluationContext add(String key, Boolean value) {
        return (EvaluationContext)super.add(key, value);
    }

    @Override
    public EvaluationContext add(String key, String value) {
        return (EvaluationContext)super.add(key, value);
    }

    @Override
    public EvaluationContext add(String key, Integer value) {
        return (EvaluationContext)super.add(key, value);
    }

    @Override
    public EvaluationContext add(String key, Double value) {
        return (EvaluationContext)super.add(key, value);
    }

    @Override
    public EvaluationContext add(String key, ZonedDateTime value) {
        return (EvaluationContext)super.add(key, value);
    }

    @Override
    public EvaluationContext add(String key, Structure value) {
        return (EvaluationContext)super.add(key, value);
    }

    @Override
    public <T> EvaluationContext add(String key, List<T> value) {
        return (EvaluationContext)super.add(key, value);
    }

}

package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public interface EvaluationContext extends Structure {
    String getTargetingKey();
    
    void setTargetingKey(String targetingKey);

    /**
     * Merges two EvaluationContext objects with the second overriding the first in
     * case of conflict.
     *
     * @param ctx1 base context
     * @param ctx2 overriding context
     * @return resulting merged context
     */
    static EvaluationContext merge(EvaluationContext ctx1, EvaluationContext ctx2) {
        if (ctx1 == null) {
            return ctx2;
        } else if (ctx2 == null) {
            return ctx1;
        }

        Map<String, Value> merged = new HashMap<String, Value>();

        merged.putAll(ctx1.asMap());
        merged.putAll(ctx2.asMap());
        EvaluationContext ec = new MutableContext(merged);

        if (ctx1.getTargetingKey() != null && !ctx1.getTargetingKey().trim().equals("")) {
            ec.setTargetingKey(ctx1.getTargetingKey());
        }

        if (ctx2.getTargetingKey() != null && !ctx2.getTargetingKey().trim().equals("")) {
            ec.setTargetingKey(ctx2.getTargetingKey());
        }

        return ec;
    }
}

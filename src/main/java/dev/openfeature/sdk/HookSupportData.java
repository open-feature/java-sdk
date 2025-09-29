package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates data for hook execution per flag evaluation.
 */
@Getter
class HookSupportData {

    private List<Pair<Hook, HookContext>> hooks;
    private EvaluationContext evaluationContext;

    @Setter
    private Map<String, Object> hints;

    HookSupportData() {}

    public void setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContext = evaluationContext;
        if (hooks != null) {
            for (Pair<Hook, HookContext> hookContextPair : hooks) {
                hookContextPair.getValue().setCtx(evaluationContext);
            }
        }
    }

    public void setHooks(List<Hook> hooks, SharedHookContext sharedContext, EvaluationContext evaluationContext) {
        List<Pair<Hook, HookContext>> hookContextPairs = new ArrayList<>();
        for (Hook hook : hooks) {
            if (hook.supportsFlagValueType(sharedContext.getType())) {
                HookContext curContext = sharedContext.hookContextFor(evaluationContext, new DefaultHookData());
                hookContextPairs.add(Pair.of(hook, curContext));
            }
        }
        this.hooks = hookContextPairs;
        this.evaluationContext = evaluationContext;
    }
}

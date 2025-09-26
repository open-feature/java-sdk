package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Encapsulates data for hook execution per flag evaluation.
 */
@Getter
class HookSupportData {

    private List<Pair<Hook, HookContext>> hooks;
    private EvaluationContext evaluationContext;
    private Map<String, Object> hints;
    private boolean isInitialized = false;

    HookSupportData() {}

    void initialize(
            List<Hook> hooks,
            SharedHookContext sharedContext,
            EvaluationContext evaluationContext,
            Map<String, Object> hints) {
        List<Pair<Hook, HookContext>> hookContextPairs = new ArrayList<>();
        for (Hook hook : hooks) {
            if (hook.supportsFlagValueType(sharedContext.getType())) {
                HookContext curContext = sharedContext.hookContextFor(evaluationContext, new DefaultHookData());
                hookContextPairs.add(Pair.of(hook, curContext));
            }
        }
        this.hooks = hookContextPairs;
        this.evaluationContext = evaluationContext;
        this.hints = hints;
        isInitialized = true;
    }

    public void setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContext = evaluationContext;
        for (Pair<Hook, HookContext> hookContextPair : hooks) {
            hookContextPair.getValue().setCtx(evaluationContext);
        }
    }
}

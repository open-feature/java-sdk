package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class HookSupport {
    private List<Pair<Hook, HookContext>> hooks;
    private EvaluationContext evaluationContext;
    private final Map<String, Object> hints;

    HookSupport(
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
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    private void setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContext = evaluationContext;
        for (Pair<Hook, HookContext> hookContextPair : hooks) {
            hookContextPair.getValue().setCtx(evaluationContext);
        }
    }

    public void executeBeforeHooks() {
        // These traverse backwards from normal.
        List<Pair<Hook, HookContext>> reversedHooks = new ArrayList<>(hooks);
        Collections.reverse(reversedHooks);

        for (Pair<Hook, HookContext> hookContextPair : reversedHooks) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();

            Optional<EvaluationContext> returnedEvalContext =
                    Optional.ofNullable(hook.before(hookContext, hints)).orElse(Optional.empty());
            if (returnedEvalContext.isPresent()) {
                // update shared evaluation context for all hooks
                setEvaluationContext(evaluationContext.merge(returnedEvalContext.get()));
            }
        }
    }

    public void executeErrorHooks(Exception error) {
        for (Pair<Hook, HookContext> hookContextPair : hooks) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();
            try {
                hook.error(hookContext, error, hints);
            } catch (Exception e) {
                log.error(
                        "Unhandled exception when running {} hook {} (only 'after' hooks should throw)",
                        "error",
                        hook.getClass(),
                        e);
            }
        }
    }

    // after hooks can throw in order to do validation
    public <T> void executeAfterHooks(FlagEvaluationDetails<T> details) {
        for (Pair<Hook, HookContext> hookContextPair : hooks) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();
            hook.after(hookContext, details, hints);
        }
    }

    public <T> void executeAfterAllHooks(FlagEvaluationDetails<T> details) {
        for (Pair<Hook, HookContext> hookContextPair : hooks) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();
            try {
                hook.finallyAfter(hookContext, details, hints);
            } catch (Exception e) {
                log.error(
                        "Unhandled exception when running {} hook {} (only 'after' hooks should throw)",
                        "finally",
                        hook.getClass(),
                        e);
            }
        }
    }
}

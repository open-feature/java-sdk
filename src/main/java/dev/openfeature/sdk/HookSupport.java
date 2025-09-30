package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class HookSupport {

    /**
     * Updates the evaluation context in the given data object's eval context and each hooks eval context.
     *
     * @param hookSupportData   the data object to modify
     * @param evaluationContext the new context to set
     */
    public void updateEvaluationContext(HookSupportData hookSupportData, EvaluationContext evaluationContext) {
        hookSupportData.evaluationContext = evaluationContext;
        if (hookSupportData.hooks != null) {
            for (Pair<Hook, HookContext> hookContextPair : hookSupportData.hooks) {
                hookContextPair.getValue().setCtx(evaluationContext);
            }
        }
    }

    /**
     * Sets the {@link Hook}-{@link HookContext}-{@link Pair} list in the given data object.
     *
     * @param hookSupportData   the data object to modify
     * @param hooks             the new hooks to set
     * @param sharedContext     the shared context across all hooks from which each hook's {@link HookContext} is
     *                          created
     * @param evaluationContext the evaluation context which is
     */
    public void setHookSupportDataHooks(
            HookSupportData hookSupportData,
            List<Hook> hooks,
            SharedHookContext sharedContext,
            EvaluationContext evaluationContext) {
        List<Pair<Hook, HookContext>> hookContextPairs = new ArrayList<>();
        for (Hook hook : hooks) {
            if (hook.supportsFlagValueType(sharedContext.getType())) {
                HookContext curContext = sharedContext.hookContextFor(evaluationContext, new DefaultHookData());
                hookContextPairs.add(Pair.of(hook, curContext));
            }
        }
        hookSupportData.hooks = hookContextPairs;
    }

    public void executeBeforeHooks(HookSupportData data) {
        // These traverse backwards from normal.
        List<Pair<Hook, HookContext>> reversedHooks = new ArrayList<>(data.getHooks());
        Collections.reverse(reversedHooks);

        for (Pair<Hook, HookContext> hookContextPair : reversedHooks) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();

            Optional<EvaluationContext> returnedEvalContext = Optional.ofNullable(
                            hook.before(hookContext, data.getHints()))
                    .orElse(Optional.empty());
            if (returnedEvalContext.isPresent()) {
                // update shared evaluation context for all hooks
                updateEvaluationContext(data, data.getEvaluationContext().merge(returnedEvalContext.get()));
            }
        }
    }

    public void executeErrorHooks(HookSupportData data, Exception error) {
        for (Pair<Hook, HookContext> hookContextPair : data.getHooks()) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();
            try {
                hook.error(hookContext, error, data.getHints());
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
    public <T> void executeAfterHooks(HookSupportData data, FlagEvaluationDetails<T> details) {
        for (Pair<Hook, HookContext> hookContextPair : data.getHooks()) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();
            hook.after(hookContext, details, data.getHints());
        }
    }

    public <T> void executeAfterAllHooks(HookSupportData data, FlagEvaluationDetails<T> details) {
        for (Pair<Hook, HookContext> hookContextPair : data.getHooks()) {
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();
            try {
                hook.finallyAfter(hookContext, details, data.getHints());
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

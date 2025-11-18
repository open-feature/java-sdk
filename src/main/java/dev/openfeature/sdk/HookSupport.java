package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class to run hooks. Initialize {@link HookSupportData} by calling setHooks, setHookContexts
 * & updateEvaluationContext in this exact order.
 */
@Slf4j
class HookSupport {

    /**
     * Sets the {@link Hook}-{@link HookContext}-{@link Pair} list in the given data object with {@link HookContext}
     * set to null. Filters hooks by supported {@link FlagValueType}.
     *
     * @param hookSupportData the data object to modify
     * @param hooks           the hooks to set
     * @param type            the flag value type to filter unsupported hooks
     */
    public void setHooks(HookSupportData hookSupportData, List<Hook> hooks, FlagValueType type) {
        List<Pair<Hook, HookContext>> hookContextPairs = new ArrayList<>();
        for (Hook hook : hooks) {
            if (hook.supportsFlagValueType(type)) {
                hookContextPairs.add(Pair.of(hook, null));
            }
        }
        hookSupportData.hooks = hookContextPairs;
    }

    /**
     * Creates & sets a {@link HookContext} for every {@link Hook}-{@link HookContext}-{@link Pair}
     * in the given data object with a new {@link HookData} instance.
     *
     * @param hookSupportData the data object to modify
     * @param sharedContext   the shared context from which the new {@link HookContext} is created
     */
    public void setHookContexts(
            HookSupportData hookSupportData,
            SharedHookContext sharedContext,
            LayeredEvaluationContext evaluationContext) {
        for (int i = 0; i < hookSupportData.hooks.size(); i++) {
            Pair<Hook, HookContext> hookContextPair = hookSupportData.hooks.get(i);
            HookContext curHookContext = sharedContext.hookContextFor(evaluationContext, new DefaultHookData());
            hookContextPair.setValue(curHookContext);
        }
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
                var returnedContext = returnedEvalContext.get();
                if (!returnedContext.isEmpty()) {
                    data.evaluationContext.putHookContext(returnedContext.asMap());
                }
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

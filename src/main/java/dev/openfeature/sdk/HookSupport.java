package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
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
     * @param hookSupportData  the data object to modify
     * @param providerHooks    the hooks filtered for the proper flag value type from the respective layer
     * @param flagOptionsHooks the hooks filtered for the proper flag value type from the respective layer
     * @param clientHooks      the hooks filtered for the proper flag value type from the respective layer
     * @param apiHooks         the hooks filtered for the proper flag value type from the respective layer
     */
    public void setHooks(
            HookSupportData hookSupportData,
            List<Hook> providerHooks,
            List<Hook> flagOptionsHooks,
            ConcurrentLinkedQueue<Hook> clientHooks,
            ConcurrentLinkedQueue<Hook> apiHooks) {
        var lengthEstimate = 0;

        if (providerHooks != null) {
            lengthEstimate += providerHooks.size();
        }
        if (flagOptionsHooks != null) {
            lengthEstimate += flagOptionsHooks.size();
        }
        if (clientHooks != null) {
            lengthEstimate += clientHooks.size();
        }
        if (apiHooks != null) {
            lengthEstimate += apiHooks.size();
        }

        ArrayList<Pair<Hook, HookContext>> hookContextPairs = new ArrayList<>(lengthEstimate);

        addAll(hookContextPairs, providerHooks);
        addAll(hookContextPairs, flagOptionsHooks);
        addAll(hookContextPairs, clientHooks);
        addAll(hookContextPairs, apiHooks);

        hookSupportData.hooks = hookContextPairs;
    }

    private void addAll(List<Pair<Hook, HookContext>> accumulator, Collection<Hook> toAdd) {
        if (toAdd == null || toAdd.isEmpty()) {
            return;
        }

        for (Hook hook : toAdd) {
            accumulator.add(Pair.of(hook, null));
        }
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
        var hooks = data.getHooks();
        for (int i = hooks.size() - 1; i >= 0; i--) {
            var hookContextPair = hooks.get(i);
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();

            Optional<EvaluationContext> returnedEvalContext = Optional.ofNullable(
                            hook.before(hookContext, data.getHints()))
                    .orElse(Optional.empty());
            if (returnedEvalContext.isPresent()) {
                var returnedContext = returnedEvalContext.get();
                // yes, we want to check for reference equality here, this prevents recursive layered contexts
                if (returnedContext != hookContext.getCtx() && !returnedContext.isEmpty()) {
                    data.evaluationContext.putHookContext(returnedContext);
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

    static void addHooks(Map<FlagValueType, ConcurrentLinkedQueue<Hook>> hookMap, Hook... hooksToAdd) {
        var types = FlagValueType.values();
        for (int i = 0; i < hooksToAdd.length; i++) {
            var current = hooksToAdd[i];
            for (int j = 0; j < types.length; j++) {
                var type = types[j];
                if (current.supportsFlagValueType(type)) {
                    hookMap.get(type).add(current);
                }
            }
        }
    }

    static ArrayList<Hook> getAllUniqueHooks(Map<FlagValueType, ConcurrentLinkedQueue<Hook>> hookMap) {
        // Hooks can be duplicated if they support multiple FlagValueTypes
        var allHooks = new HashSet<Hook>();
        for (var queue : hookMap.values()) {
            allHooks.addAll(queue);
        }
        return new ArrayList<>(allHooks);
    }
}

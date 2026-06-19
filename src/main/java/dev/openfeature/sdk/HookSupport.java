package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Collection;
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
     * Sources are iterated in order: provider, options, client, API (reversed for the {@code before} stage by
     * {@link #executeBeforeHooks}).
     *
     * <p>The four hook sources are accepted as separate collections to avoid allocation on the evaluation hot path.
     *
     * @param hookSupportData the data object to modify
     * @param providerHooks   provider-level hooks
     * @param optionHooks     per-evaluation option hooks
     * @param clientHooks     client-level hooks
     * @param apiHooks        API-level hooks
     * @param type            the flag value type to filter unsupported hooks
     */
    public void setHooks(
            HookSupportData hookSupportData,
            Collection<Hook> providerHooks,
            Collection<Hook> optionHooks,
            Collection<Hook> clientHooks,
            Collection<Hook> apiHooks,
            FlagValueType type) {
        List<Pair<Hook, HookContext>> hookContextPairs = new ArrayList<>();
        addFilteredHooks(hookContextPairs, providerHooks, type);
        addFilteredHooks(hookContextPairs, optionHooks, type);
        addFilteredHooks(hookContextPairs, clientHooks, type);
        addFilteredHooks(hookContextPairs, apiHooks, type);
        hookSupportData.hooks = hookContextPairs;
    }

    private static void addFilteredHooks(
            List<Pair<Hook, HookContext>> dest, Collection<Hook> source, FlagValueType type) {
        if (source.isEmpty()) {
            return;
        }
        for (Hook hook : source) {
            if (hook.supportsFlagValueType(type)) {
                dest.add(Pair.of(hook, null));
            }
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

    // S2789: Hook is user-implemented; defensive null check against non-conforming impls returning null.
    @SuppressWarnings("java:S2789")
    public void executeBeforeHooks(HookSupportData data) {
        // These traverse backwards from normal.
        List<Pair<Hook, HookContext>> hooks = data.getHooks();
        for (int i = hooks.size() - 1; i >= 0; i--) {
            var hookContextPair = hooks.get(i);
            var hook = hookContextPair.getKey();
            var hookContext = hookContextPair.getValue();

            Optional<EvaluationContext> returnedEvalContext = hook.before(hookContext, data.getHints());
            if (returnedEvalContext != null && returnedEvalContext.isPresent()) {
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
}

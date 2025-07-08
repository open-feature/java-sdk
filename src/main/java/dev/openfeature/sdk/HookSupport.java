package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
class HookSupport {

    public EvaluationContext beforeHooks(
            FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks, Map<String, Object> hints) {
        return callBeforeHooks(flagValueType, hookCtx, hooks, hints);
    }

    public void afterHooks(
            FlagValueType flagValueType,
            HookContext hookContext,
            FlagEvaluationDetails details,
            List<Hook> hooks,
            Map<String, Object> hints) {
        executeHooksUnchecked(flagValueType, hooks, hook -> hook.after(hookContext, details, hints));
    }

    public void afterAllHooks(
            FlagValueType flagValueType,
            HookContext hookCtx,
            FlagEvaluationDetails details,
            List<Hook> hooks,
            Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "finally", hook -> hook.finallyAfter(hookCtx, details, hints));
    }

    public void errorHooks(
            FlagValueType flagValueType,
            HookContext hookCtx,
            Exception e,
            List<Hook> hooks,
            Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "error", hook -> hook.error(hookCtx, e, hints));
    }

    private <T> void executeHooks(
            FlagValueType flagValueType, List<Hook> hooks, String hookMethod, Consumer<Hook<T>> hookCode) {
        if (hooks != null) {
            for (Hook hook : hooks) {
                if (hook.supportsFlagValueType(flagValueType)) {
                    executeChecked(hook, hookCode, hookMethod);
                }
            }
        }
    }

    // before, error, and finally hooks shouldn't throw
    private <T> void executeChecked(Hook<T> hook, Consumer<Hook<T>> hookCode, String hookMethod) {
        try {
            hookCode.accept(hook);
        } catch (Exception exception) {
            log.error(
                    "Unhandled exception when running {} hook {} (only 'after' hooks should throw)",
                    hookMethod,
                    hook.getClass(),
                    exception);
        }
    }

    // after hooks can throw in order to do validation
    private <T> void executeHooksUnchecked(FlagValueType flagValueType, List<Hook> hooks, Consumer<Hook<T>> hookCode) {
        if (hooks != null) {
            for (Hook hook : hooks) {
                if (hook.supportsFlagValueType(flagValueType)) {
                    hookCode.accept(hook);
                }
            }
        }
    }

    private EvaluationContext callBeforeHooks(
            FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks, Map<String, Object> hints) {
        // These traverse backwards from normal.
        List<Hook> reversedHooks = new ArrayList<>(hooks);
        Collections.reverse(reversedHooks);
        EvaluationContext context = hookCtx.getCtx();

        // Create hook data for each hook instance
        Map<Hook, HookData> hookDataMap = new HashMap<>();
        for (Hook hook : reversedHooks) {
            if (hook.supportsFlagValueType(flagValueType)) {
                hookDataMap.put(hook, HookData.create());
            }
        }

        for (Hook hook : reversedHooks) {
            if (hook.supportsFlagValueType(flagValueType)) {
                // Create a new context with this hook's data
                HookContext contextWithHookData = hookCtx.withHookData(hookDataMap.get(hook));
                Optional<EvaluationContext> optional = Optional.ofNullable(hook.before(contextWithHookData, hints))
                        .orElse(Optional.empty());
                if (optional.isPresent()) {
                    context = context.merge(optional.get());
                }
            }
        }
        return context;
    }
}

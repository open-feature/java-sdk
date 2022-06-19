package dev.openfeature.javasdk;

import java.util.*;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import lombok.*;
import org.slf4j.Logger;

@RequiredArgsConstructor
class HookSupport {

    private final Logger log;

    public void errorHooks(FlagValueType flagValueType, HookContext hookCtx, Exception e, List<Hook> hooks, Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "error", hook -> hook.error(hookCtx, e, hints));
    }

    public void afterAllHooks(FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks, Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "finally", hook -> hook.finallyAfter(hookCtx, hints));
    }

    public void afterHooks(FlagValueType flagValueType, HookContext hookContext, FlagEvaluationDetails details, List<Hook> hooks, Map<String, Object> hints) {
        executeHooksUnsafe(flagValueType, hooks, hook -> hook.after(hookContext, details, hints));
    }

    private <T> void executeHooks(
        FlagValueType flagValueType, List<Hook> hooks,
        String hookMethod,
        Consumer<Hook<T>> hookCode
    ) {
        hooks
            .stream()
            .filter(hook -> hook.supportsFlagValueType() == flagValueType)
            .forEach(hook -> executeChecked(hook, hookCode, hookMethod));
    }

    private <T> void executeHooksUnsafe(
        FlagValueType flagValueType, List<Hook> hooks,
        Consumer<Hook<T>> hookCode
    ) {
        hooks
            .stream()
            .filter(hook -> hook.supportsFlagValueType() == flagValueType)
            .forEach(hookCode::accept);
    }

    private <T> void executeChecked(Hook<T> hook, Consumer<Hook<T>> hookCode, String hookMethod) {
        try {
            hookCode.accept(hook);
        } catch (Exception exception) {
            log.error("Exception when running {} hooks {}", hookMethod, hook.getClass(), exception);
        }
    }

    public EvaluationContext beforeHooks(HookContext hookCtx, List<Hook> hooks, Map<String, Object> hints) {
        // These traverse backwards from normal.
        EvaluationContext ctx = hookCtx.getCtx();
        for (Hook hook : Lists.reverse(hooks)) {
            Optional<EvaluationContext> newCtx = hook.before(hookCtx, hints);
            if (newCtx != null && newCtx.isPresent()) {
                ctx = EvaluationContext.merge(ctx, newCtx.get());
                hookCtx = hookCtx.withCtx(ctx);
            }
        }
        return ctx;
    }

}

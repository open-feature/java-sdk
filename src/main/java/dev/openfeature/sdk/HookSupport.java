package dev.openfeature.sdk;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
class HookSupport {

    public EvaluationContext beforeHooks(FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks,
            Map<String, Object> hints) {
        Stream<EvaluationContext> result = callBeforeHooks(flagValueType, hookCtx, hooks, hints);
        return hookCtx.getCtx().merge(
                result.reduce(hookCtx.getCtx(), (EvaluationContext accumulated, EvaluationContext current) -> {
                    return accumulated.merge(current);
                }));
    }

    public void afterHooks(FlagValueType flagValueType, HookContext hookContext, FlagEvaluationDetails details,
            List<Hook> hooks, Map<String, Object> hints) {
        executeHooksUnchecked(flagValueType, hooks, hook -> hook.after(hookContext, details, hints));
    }

    public void afterAllHooks(FlagValueType flagValueType, HookContext hookCtx, List<Hook> hooks,
            Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "finally", hook -> hook.finallyAfter(hookCtx, hints));
    }

    public void errorHooks(FlagValueType flagValueType, HookContext hookCtx, Exception e, List<Hook> hooks,
            Map<String, Object> hints) {
        executeHooks(flagValueType, hooks, "error", hook -> hook.error(hookCtx, e, hints));
    }

    private <T> void executeHooks(
            FlagValueType flagValueType, List<Hook> hooks,
            String hookMethod,
            Consumer<Hook<T>> hookCode) {
        if (hooks != null) {
            hooks
                    .stream()
                    .filter(hook -> hook.supportsFlagValueType(flagValueType))
                    .forEach(hook -> executeChecked(hook, hookCode, hookMethod));
        }
    }

    // before, error, and finally hooks shouldn't throw
    private <T> void executeChecked(Hook<T> hook, Consumer<Hook<T>> hookCode, String hookMethod) {
        try {
            hookCode.accept(hook);
        } catch (Exception exception) {
            log.error("Unhandled exception when running {} hook {} (only 'after' hooks should throw)", hookMethod,
                    hook.getClass(), exception);
        }
    }

    // after hooks can throw in order to do validation
    private <T> void executeHooksUnchecked(
            FlagValueType flagValueType, List<Hook> hooks,
            Consumer<Hook<T>> hookCode) {
        if (hooks != null) {
            hooks
                    .stream()
                    .filter(hook -> hook.supportsFlagValueType(flagValueType))
                    .forEach(hookCode::accept);
        }
    }

    private Stream<EvaluationContext> callBeforeHooks(FlagValueType flagValueType, HookContext hookCtx,
            List<Hook> hooks, Map<String, Object> hints) {
        // These traverse backwards from normal.
        List<Hook> reversedHooks = IntStream
                .range(0, hooks.size())
                .map(i -> hooks.size() - 1 - i)
                .mapToObj(hooks::get)
                .collect(Collectors.toList());

        return reversedHooks
                .stream()
                .filter(hook -> hook.supportsFlagValueType(flagValueType))
                .map(hook -> hook.before(hookCtx, hints))
                .filter(Objects::nonNull)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(EvaluationContext.class::cast);
    }
}

package dev.openfeature.sdk;

import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Hook;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.FlagEvaluationDetails;
import dev.openfeature.api.lifecycle.HookContext;
import dev.openfeature.api.lifecycle.HookData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unchecked", "rawtypes"})
class HookSupport {

    private static final Logger log = LoggerFactory.getLogger(HookSupport.class);

    public EvaluationContext beforeHooks(
            FlagValueType flagValueType,
            HookContext hookCtx,
            List<Pair<Hook<?>, HookData>> hookDataPairs,
            Map<String, Object> hints) {
        return callBeforeHooks(flagValueType, hookCtx, hookDataPairs, hints);
    }

    public void afterHooks(
            FlagValueType flagValueType,
            HookContext hookContext,
            FlagEvaluationDetails details,
            List<Pair<Hook<?>, HookData>> hookDataPairs,
            Map<String, Object> hints) {
        executeHooksUnchecked(
                flagValueType, hookDataPairs, hookContext, (hook, ctx) -> hook.after(ctx, details, hints));
    }

    public void afterAllHooks(
            FlagValueType flagValueType,
            HookContext hookCtx,
            FlagEvaluationDetails details,
            List<Pair<Hook<?>, HookData>> hookDataPairs,
            Map<String, Object> hints) {
        executeHooks(
                flagValueType,
                hookDataPairs,
                hookCtx,
                "finally",
                (hook, ctx) -> hook.finallyAfter(ctx, details, hints));
    }

    public void errorHooks(
            FlagValueType flagValueType,
            HookContext hookCtx,
            Exception e,
            List<Pair<Hook<?>, HookData>> hookDataPairs,
            Map<String, Object> hints) {
        executeHooks(flagValueType, hookDataPairs, hookCtx, "error", (hook, ctx) -> hook.error(ctx, e, hints));
    }

    public List<Pair<Hook<?>, HookData>> getHookDataPairs(List<Hook<?>> hooks, FlagValueType flagValueType) {
        var pairs = new ArrayList<Pair<Hook<?>, HookData>>();
        for (Hook hook : hooks) {
            if (hook.supportsFlagValueType(flagValueType)) {
                pairs.add(Pair.of(hook, HookData.create()));
            }
        }
        return pairs;
    }

    private <T> void executeHooks(
            FlagValueType flagValueType,
            List<Pair<Hook<?>, HookData>> hookDataPairs,
            HookContext hookContext,
            String hookMethod,
            BiConsumer<Hook<T>, HookContext> hookCode) {
        if (hookDataPairs != null) {
            for (Pair<Hook<?>, HookData> hookDataPair : hookDataPairs) {
                Hook hook = hookDataPair.getLeft();
                HookData hookData = hookDataPair.getRight();
                executeChecked(hook, hookData, hookContext, hookCode, hookMethod);
            }
        }
    }

    // before, error, and finally hooks shouldn't throw
    private <T> void executeChecked(
            Hook<T> hook,
            HookData hookData,
            HookContext hookContext,
            BiConsumer<Hook<T>, HookContext> hookCode,
            String hookMethod) {
        try {
            var hookCtxWithData = HookContextWithData.of(hookContext, hookData);
            hookCode.accept(hook, hookCtxWithData);
        } catch (Exception exception) {
            log.error(
                    "Unhandled exception when running {} hook {} (only 'after' hooks should throw)",
                    hookMethod,
                    hook.getClass(),
                    exception);
        }
    }

    // after hooks can throw in order to do validation
    private <T> void executeHooksUnchecked(
            FlagValueType flagValueType,
            List<Pair<Hook<?>, HookData>> hookDataPairs,
            HookContext hookContext,
            BiConsumer<Hook<T>, HookContext> hookCode) {
        if (hookDataPairs != null) {
            for (Pair<Hook<?>, HookData> hookDataPair : hookDataPairs) {
                Hook hook = hookDataPair.getLeft();
                HookData hookData = hookDataPair.getRight();
                var hookCtxWithData = HookContextWithData.of(hookContext, hookData);
                hookCode.accept(hook, hookCtxWithData);
            }
        }
    }

    private EvaluationContext callBeforeHooks(
            FlagValueType flagValueType,
            HookContext hookCtx,
            List<Pair<Hook<?>, HookData>> hookDataPairs,
            Map<String, Object> hints) {
        // These traverse backwards from normal.
        List<Pair<Hook<?>, HookData>> reversedHooks = new ArrayList<>(hookDataPairs);
        Collections.reverse(reversedHooks);
        EvaluationContext context = hookCtx.getCtx();

        for (Pair<Hook<?>, HookData> hookDataPair : reversedHooks) {
            Hook hook = hookDataPair.getLeft();
            HookData hookData = hookDataPair.getRight();

            // Create a new context with this hook's data
            HookContext contextWithHookData = HookContextWithData.of(hookCtx, hookData);
            Optional<EvaluationContext> optional =
                    Optional.ofNullable(hook.before(contextWithHookData, hints)).orElse(Optional.empty());
            if (optional.isPresent()) {
                context = context.merge(optional.get());
            }
        }
        return context;
    }
}

package dev.openfeature.sdk;

import java.util.Map;
import java.util.Optional;

class TestHookWithData implements Hook {
    private final Object value;
    HookData hookData = null;

    public TestHookWithData(Object value) {
        this.value = value;
    }

    public TestHookWithData() {
        this("test");
    }

    @Override
    public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
        ctx.getHookData().set("before", value);
        hookData = ctx.getHookData();
        return Optional.empty();
    }

    @Override
    public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        ctx.getHookData().set("after", value);
        hookData = ctx.getHookData();
    }

    @Override
    public void error(HookContext ctx, Exception error, Map hints) {
        ctx.getHookData().set("error", value);
        hookData = ctx.getHookData();
    }

    @Override
    public void finallyAfter(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        ctx.getHookData().set("finallyAfter", value);
        hookData = ctx.getHookData();
    }
}

package dev.openfeature.sdk;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class TypedTestHook implements Hook {
    public final FlagValueType flagValueType;
    public final AtomicBoolean beforeCalled = new AtomicBoolean(false);
    public final AtomicBoolean afterCalled = new AtomicBoolean(false);
    public final AtomicBoolean errorCalled = new AtomicBoolean(false);
    public final AtomicBoolean finallyAfterCalled = new AtomicBoolean(false);

    public TypedTestHook(FlagValueType flagValueType) {
        this.flagValueType = flagValueType;
    }

    @Override
    public boolean supportsFlagValueType(FlagValueType flagValueType) {
        return this.flagValueType == flagValueType;
    }

    @Override
    public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
        beforeCalled.set(true);
        return Optional.empty();
    }

    @Override
    public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        afterCalled.set(true);
    }

    @Override
    public void error(HookContext ctx, Exception error, Map hints) {
        errorCalled.set(true);
    }

    @Override
    public void finallyAfter(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        finallyAfterCalled.set(true);
    }
}

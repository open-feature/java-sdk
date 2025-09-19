package dev.openfeature.sdk.e2e;

import dev.openfeature.api.Hook;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.FlagEvaluationDetails;
import dev.openfeature.api.lifecycle.HookContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MockHook implements Hook {
    private boolean beforeCalled;

    private boolean afterCalled;

    private boolean errorCalled;

    private boolean finallyAfterCalled;

    private final Map<String, FlagEvaluationDetails> evaluationDetails = new HashMap<>();

    @Override
    public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
        beforeCalled = true;
        return Optional.of(ctx.getCtx());
    }

    @Override
    public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        afterCalled = true;
        evaluationDetails.put("after", details);
    }

    @Override
    public void error(HookContext ctx, Exception error, Map hints) {
        errorCalled = true;
    }

    @Override
    public void finallyAfter(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        finallyAfterCalled = true;
        evaluationDetails.put("finally", details);
    }

    public boolean isBeforeCalled() {
        return beforeCalled;
    }

    public boolean isAfterCalled() {
        return afterCalled;
    }

    public boolean isErrorCalled() {
        return errorCalled;
    }

    public boolean isFinallyAfterCalled() {
        return finallyAfterCalled;
    }

    public Map<String, FlagEvaluationDetails> getEvaluationDetails() {
        return evaluationDetails;
    }
}

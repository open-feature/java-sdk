package dev.openfeature.sdk.e2e;

import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.api.Hook;
import dev.openfeature.api.HookContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

public class MockHook implements Hook {
    @Getter
    private boolean beforeCalled;

    @Getter
    private boolean afterCalled;

    @Getter
    private boolean errorCalled;

    @Getter
    private boolean finallyAfterCalled;

    @Getter
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
}

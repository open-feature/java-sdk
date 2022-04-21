package javasdk;

import lombok.Getter;

public class NoOpProvider<T extends EvaluationContext> implements FeatureProvider {
    @Getter
    private String name = "No-op Provider";

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return new ProviderEvaluation<Boolean>(defaultValue, Reason.DEFAULT);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return new ProviderEvaluation<String>(defaultValue, Reason.DEFAULT);
    }

    @Override
    public ProviderEvaluation<Long> getNumberEvaluation(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return new ProviderEvaluation<Long>(defaultValue, Reason.DEFAULT);
    }
}

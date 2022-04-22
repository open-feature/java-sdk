package javasdk;

import lombok.Getter;

public class NoOpProvider<T extends EvaluationContext> implements FeatureProvider {
    @Getter
    private final String name = "No-op Provider";

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<Long> getNumberEvaluation(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<Long>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT)
                .build();
    }
}

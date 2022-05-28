package dev.openfeature.javasdk;

import lombok.Getter;

public class NoOpProvider implements FeatureProvider {
    @Getter
    private final String name = "No-op Provider";

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .variant("Passed in default")
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .variant("Passed in default")
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .variant("Passed in default")
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public <T> ProviderEvaluation<T> getObjectEvaluation(String key, T defaultValue, EvaluationContext invocationContext, FlagEvaluationOptions options) {
        return ProviderEvaluation.<T>builder()
                .value(defaultValue)
                .variant("Passed in default")
                .reason(Reason.DEFAULT)
                .build();
    }
}

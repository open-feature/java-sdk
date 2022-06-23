package dev.openfeature.javasdk;

public class DoSomethingProvider implements FeatureProvider {

    @Override
    public Metadata getMetadata() {
        return () -> "test";
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<Boolean>builder()
                .value(!defaultValue).build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<String>builder()
                .value(new StringBuilder(defaultValue).reverse().toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue * 100)
                .build();
    }

    @Override
    public <T> ProviderEvaluation<T> getObjectEvaluation(String key, T defaultValue, EvaluationContext invocationContext, FlagEvaluationOptions options) {
        return ProviderEvaluation.<T>builder()
                .value(null)
                .build();
    }
}

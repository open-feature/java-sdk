package dev.openfeature.javasdk;

public class DoSomethingProvider implements FeatureProvider {

    private EvaluationContext savedContext;

    public EvaluationContext getMergedContext() {
        return savedContext;
    }

    @Override
    public Metadata getMetadata() {
        return () -> "test";
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        savedContext = ctx;
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
        savedContext = ctx;
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue * 100)
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        savedContext = ctx;
        return ProviderEvaluation.<Double>builder()
                .value(defaultValue * 100)
                .build();
    }

    @Override
    public ProviderEvaluation<Structure> getObjectEvaluation(String key, Structure defaultValue, EvaluationContext invocationContext, FlagEvaluationOptions options) {
        savedContext = invocationContext;
        return ProviderEvaluation.<Structure>builder()
                .value(null)
                .build();
    }
}

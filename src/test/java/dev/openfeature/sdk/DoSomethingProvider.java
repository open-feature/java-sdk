package dev.openfeature.sdk;

class DoSomethingProvider implements FeatureProvider {

    static final String name = "Something";
    // Flag evaluation metadata
    static final FlagMetadata flagMetadata = FlagMetadata.builder().build();

    private EvaluationContext savedContext;

    EvaluationContext getMergedContext() {
        return savedContext;
    }

    @Override
    public Metadata getMetadata() {
        return () -> name;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        savedContext = ctx;
        return ProviderEvaluation.<Boolean>builder()
                .value(!defaultValue)
                .flagMetadata(flagMetadata)
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<String>builder()
                .value(new StringBuilder(defaultValue).reverse().toString())
                .flagMetadata(flagMetadata)
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        savedContext = ctx;
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue * 100)
                .flagMetadata(flagMetadata)
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        savedContext = ctx;
        return ProviderEvaluation.<Double>builder()
                .value(defaultValue * 100)
                .flagMetadata(flagMetadata)
                .build();
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext invocationContext) {
        savedContext = invocationContext;
        return ProviderEvaluation.<Value>builder()
                .value(null)
                .flagMetadata(flagMetadata)
                .build();
    }
}

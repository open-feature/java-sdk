package dev.openfeature.javasdk;


public class AlwaysBrokenProvider implements FeatureProvider {
    @Override
    public String getName() {
        throw new NotImplementedException();
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        throw new NotImplementedException();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        throw new NotImplementedException();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        throw new NotImplementedException();
    }

    @Override
    public <T> ProviderEvaluation<T> getObjectEvaluation(String key, T defaultValue, EvaluationContext invocationContext, FlagEvaluationOptions options) {
        throw new NotImplementedException();
    }
}

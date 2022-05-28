package dev.openfeature.javasdk;

public interface FeatureProvider {
    String getName();
    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
    ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
    <T> ProviderEvaluation<T> getObjectEvaluation(String key, T defaultValue, EvaluationContext invocationContext, FlagEvaluationOptions options);
}

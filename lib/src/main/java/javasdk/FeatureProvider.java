package javasdk;

public interface FeatureProvider {
    // TODO: Figure out transforming vs non-transforming contexsts
    String getName();
    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
    ProviderEvaluation<Long> getNumberEvaluation(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
}

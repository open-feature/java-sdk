package dev.openfeature.javasdk;

import java.util.ArrayList;
import java.util.List;

/**
 * The interface implemented by upstream flag providers to resolve flags for their service.
 */
public interface FeatureProvider {
    Metadata getMetadata();

    default List<Hook> getProviderHooks() {
        return new ArrayList<>();
    }

    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx,
                                                     FlagEvaluationOptions options);

    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx,
                                                   FlagEvaluationOptions options);

    ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx,
                                                     FlagEvaluationOptions options);

    ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx,
                                                   FlagEvaluationOptions options);

    <T> ProviderEvaluation<T> getObjectEvaluation(String key, T defaultValue, EvaluationContext invocationContext,
                                                  FlagEvaluationOptions options);
}

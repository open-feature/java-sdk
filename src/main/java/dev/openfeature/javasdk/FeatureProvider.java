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

    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx);

    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx);
}

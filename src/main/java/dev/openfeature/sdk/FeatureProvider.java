package dev.openfeature.sdk;

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

    /**
     * This method is called before a provider is used to evaluate flags. Providers can overwrite this method,
     * if they have special initialization needed prior being called for flag evaluation.
     * <p>
     * It is ok, if the method is expensive as it is executed in the background. All runtime exceptions will be
     * caught and logged.
     * </p>
     */
    default void initialize() {
        // Intentionally left blank
    }

    /**
     * This method is called when a new provider is about to be used to evaluate flags.
     * Providers can overwrite this method, if they have special shutdown actions needed.
     * <p>
     * It is ok, if the method is expensive as it is executed in the background. All runtime exceptions will be
     * caught and logged.
     * </p>
     */
    default void shutdown() {
        // Intentionally left blank
    }

}

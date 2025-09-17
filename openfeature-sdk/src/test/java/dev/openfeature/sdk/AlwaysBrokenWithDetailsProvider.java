package dev.openfeature.sdk;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.FeatureProvider;
import dev.openfeature.api.ProviderEvaluation;
import dev.openfeature.api.ProviderMetadata;
import dev.openfeature.api.Value;

public class AlwaysBrokenWithDetailsProvider implements FeatureProvider {

    private final String name = "always broken with details";

    @Override
    public ProviderMetadata getMetadata() {
        return () -> name;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of( ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of( ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of( ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext invocationContext) {

        return ProviderEvaluation.of(ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE);
    }
}

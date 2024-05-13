package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.FlagNotFoundError;

public class AlwaysBrokenProvider implements FeatureProvider {

    @Override
    public Metadata getMetadata() {
        return () -> {
            throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
        };
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext invocationContext) {
        throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
    }
}

package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.FatalError;
import dev.openfeature.sdk.exceptions.GeneralError;

public class FatalErrorProvider implements FeatureProvider {

    private final String name = "fatal";

    @Override
    public Metadata getMetadata() {
        return () -> name;
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        throw new FatalError(); // throw a fatal error on startup (this will cause the SDK to short circuit evaluations)
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        throw new GeneralError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        throw new GeneralError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        throw new GeneralError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        throw new GeneralError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext invocationContext) {
        throw new GeneralError(TestConstants.BROKEN_MESSAGE);
    }
}

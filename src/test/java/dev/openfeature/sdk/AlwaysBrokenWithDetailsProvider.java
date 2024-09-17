package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.FlagNotFoundError;

public class AlwaysBrokenWithDetailsProvider implements FeatureProvider {

    @Override
    public Metadata getMetadata() {
        return () -> {
            throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
        };
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Boolean>builder()
                .errorMessage(TestConstants.BROKEN_MESSAGE)
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<String>builder()
                .errorMessage(TestConstants.BROKEN_MESSAGE)
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Integer>builder()
                .errorMessage(TestConstants.BROKEN_MESSAGE)
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Double>builder()
                .errorMessage(TestConstants.BROKEN_MESSAGE)
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext invocationContext) {
        return ProviderEvaluation.<Value>builder()
                .errorMessage(TestConstants.BROKEN_MESSAGE)
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
    }
}

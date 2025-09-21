package dev.openfeature.sdk;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.Provider;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.types.Metadata;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;

public class AlwaysBrokenWithDetailsProvider implements Provider {

    private final String name = "always broken with details";

    @Override
    public ProviderMetadata getMetadata() {
        return () -> name;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE, Metadata.EMPTY);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE, Metadata.EMPTY);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE, Metadata.EMPTY);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE, Metadata.EMPTY);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext invocationContext) {

        return ProviderEvaluation.of(ErrorCode.FLAG_NOT_FOUND, TestConstants.BROKEN_MESSAGE, Metadata.EMPTY);
    }
}

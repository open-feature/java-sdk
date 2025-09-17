package dev.openfeature.sdk.e2e;

import dev.openfeature.api.*;
import dev.openfeature.api.ProviderMetadata;

public class ContextStoringProvider implements FeatureProvider {
    private EvaluationContext evaluationContext;

    @Override
    public ProviderMetadata getMetadata() {
        return () -> getClass().getSimpleName();
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        this.evaluationContext = ctx;
        return null;
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        this.evaluationContext = ctx;
        return null;
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        this.evaluationContext = ctx;
        return null;
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        this.evaluationContext = ctx;
        return null;
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        this.evaluationContext = ctx;
        return null;
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }
}

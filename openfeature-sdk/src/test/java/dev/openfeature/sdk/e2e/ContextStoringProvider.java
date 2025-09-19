package dev.openfeature.sdk.e2e;

import dev.openfeature.api.Provider;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;

public class ContextStoringProvider implements Provider {
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

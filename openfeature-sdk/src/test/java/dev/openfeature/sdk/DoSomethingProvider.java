package dev.openfeature.sdk;

import dev.openfeature.api.Provider;
import dev.openfeature.api.Reason;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.types.Metadata;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;

class DoSomethingProvider implements Provider {

    static final String name = "Something";
    // Flag evaluation metadata
    static final Metadata DEFAULT_METADATA = Metadata.EMPTY;
    private Metadata flagMetadata;

    public DoSomethingProvider() {
        this.flagMetadata = DEFAULT_METADATA;
    }

    public DoSomethingProvider(Metadata flagMetadata) {
        this.flagMetadata = flagMetadata;
    }

    @Override
    public ProviderMetadata getMetadata() {
        return () -> name;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(!defaultValue, null, Reason.DEFAULT.toString(), flagMetadata);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(
                new StringBuilder(defaultValue).reverse().toString(), null, Reason.DEFAULT.toString(), flagMetadata);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(defaultValue * 100, null, Reason.DEFAULT.toString(), flagMetadata);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(defaultValue * 100, null, Reason.DEFAULT.toString(), flagMetadata);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext invocationContext) {
        return ProviderEvaluation.of(null, null, Reason.DEFAULT.toString(), flagMetadata);
    }
}

package dev.openfeature.sdk;

import dev.openfeature.api.Hook;
import dev.openfeature.api.Provider;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.exceptions.FlagNotFoundError;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;
import java.util.List;

public class AlwaysBrokenWithExceptionProvider implements Provider {

    private final String name = "always broken";

    @Override
    public ProviderMetadata getMetadata() {
        return () -> name;
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
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext invocationContext) {
        throw new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
    }

    @Override
    public Provider addHooks(Hook<?>... hooks) {
        return this;
    }

    @Override
    public List<Hook<?>> getHooks() {
        return List.of();
    }
}

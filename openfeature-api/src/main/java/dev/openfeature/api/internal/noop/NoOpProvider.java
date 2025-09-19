package dev.openfeature.api.internal.noop;

import dev.openfeature.api.Hook;
import dev.openfeature.api.Provider;
import dev.openfeature.api.Reason;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;
import java.util.List;

/**
 * A {@link Provider} that simply returns the default values passed to it.
 *
 * <p><strong>This is an internal implementation class and should not be used directly by external users.</strong>
 */
@ExcludeFromGeneratedCoverageReport
public class NoOpProvider implements Provider {
    public static final String PASSED_IN_DEFAULT = "Passed in default";

    private final String name = "No-op Provider";

    public String getName() {
        return name;
    }

    @Override
    public ProviderMetadata getMetadata() {
        return () -> name;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(defaultValue, PASSED_IN_DEFAULT, Reason.DEFAULT.toString(), null);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(defaultValue, PASSED_IN_DEFAULT, Reason.DEFAULT.toString(), null);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(defaultValue, PASSED_IN_DEFAULT, Reason.DEFAULT.toString(), null);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.of(defaultValue, PASSED_IN_DEFAULT, Reason.DEFAULT.toString(), null);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext invocationContext) {
        return ProviderEvaluation.of(defaultValue, PASSED_IN_DEFAULT, Reason.DEFAULT.toString(), null);
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

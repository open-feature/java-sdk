package dev.openfeature.api.internal.noop;

import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.FeatureProvider;
import dev.openfeature.api.ProviderEvaluation;
import dev.openfeature.api.ProviderMetadata;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.Reason;
import dev.openfeature.api.Value;
import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;

/**
 * A {@link FeatureProvider} that simply returns the default values passed to it.
 *
 * <p><strong>This is an internal implementation class and should not be used directly by external users.</strong>
 */
@ExcludeFromGeneratedCoverageReport
public class NoOpProvider implements FeatureProvider {
    public static final String PASSED_IN_DEFAULT = "Passed in default";

    private final String name = "No-op Provider";

    public String getName() {
        return name;
    }

    // The Noop provider is ALWAYS NOT_READY, otherwise READY handlers would run immediately when attached.
    @Override
    public ProviderState getState() {
        return ProviderState.NOT_READY;
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
}

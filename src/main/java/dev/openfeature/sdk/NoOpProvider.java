package dev.openfeature.sdk;

import lombok.Getter;

/**
 * A {@link FeatureProvider} that simply returns the default values passed to it.
 */
public class NoOpProvider implements FeatureProvider {
    public static final String PASSED_IN_DEFAULT = "Passed in default";
    @Getter
    private final String name = "No-op Provider";

    // The Noop provider is ALWAYS NOT_READY, otherwise READY handlers would run immediately when attached.
    @Override
    public ProviderState getState() {
        return ProviderState.NOT_READY;
    }

    @Override
    public Metadata getMetadata() {
        return () -> name;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
            EvaluationContext invocationContext) {
        return ProviderEvaluation.<Value>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }
}

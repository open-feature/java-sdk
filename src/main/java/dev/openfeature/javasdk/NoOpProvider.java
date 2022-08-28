package dev.openfeature.javasdk;

import lombok.Getter;

/**
 * A {@link FeatureProvider} that simply returns the default values passed to it.
 */
public class NoOpProvider implements FeatureProvider {
    public static final String PASSED_IN_DEFAULT = "Passed in default";
    @Getter
    private final String name = "No-op Provider";

    @Override
    public Metadata getMetadata() {
        return new Metadata() {
            @Override
            public String getName() {
                return name;
            }
        };
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx,
                                                            FlagEvaluationOptions options) {
        return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx,
                                                          FlagEvaluationOptions options) {
        return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx,
                                                            FlagEvaluationOptions options) {
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx,
                                                          FlagEvaluationOptions options) {
        return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT)
                .build();
    }

    @Override
    public ProviderEvaluation<Structure> getObjectEvaluation(String key, Structure defaultValue,
                                                         EvaluationContext invocationContext,
                                                         FlagEvaluationOptions options) {
        return ProviderEvaluation.<Structure>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT)
                .build();
    }
}

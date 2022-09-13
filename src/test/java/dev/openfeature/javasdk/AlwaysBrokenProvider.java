package dev.openfeature.javasdk;


public class AlwaysBrokenProvider implements FeatureProvider {
    @Override
    public Metadata getMetadata() {
        return new Metadata() {
            @Override
            public String getName() {
                throw new NotImplementedException("BORK");
            }
        };
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        throw new NotImplementedException("BORK");
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        throw new NotImplementedException("BORK");
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        throw new NotImplementedException("BORK");
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        throw new NotImplementedException("BORK");
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext invocationContext) {
        throw new NotImplementedException("BORK");
    }
}

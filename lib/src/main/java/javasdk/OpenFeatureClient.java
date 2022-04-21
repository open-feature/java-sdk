package javasdk;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class OpenFeatureClient implements Client {
    private final OpenFeatureAPI openfeatureApi;
    @Getter private String name;
    @Getter private String version;
    @Getter private List<Hook> hooks;

    public OpenFeatureClient(OpenFeatureAPI openFeatureAPI, String name, String version) {
        this.openfeatureApi = openFeatureAPI;
        this.name = name;
        this.version = version;
    }

    @Override
    public void registerHooks(Hook... hooks) {
        this.hooks.addAll(Arrays.asList(hooks));
    }

    private <T> FlagEvaluationDetails<T> evaluateFlag(FlagValueType type, String key, T defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        FeatureProvider provider = this.openfeatureApi.getProvider();
        // TODO: We should actually run the relevant hooks.

        final ProviderEvaluation<T> providerEval;
        if (type == FlagValueType.BOOLEAN) {
            // TODO: Can we guarantee that defaultValue is a boolean? If not, how to we handle that?
            providerEval = (ProviderEvaluation<T>) provider.getBooleanEvaluation(key, (Boolean) defaultValue, ctx, options);
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
        return FlagEvaluationDetails.from(providerEval, key, null);
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return (Boolean) getBooleanDetails(key, defaultValue).getValue();
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx) {
        return (Boolean) getBooleanDetails(key, defaultValue, ctx).getValue();
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return (Boolean) getBooleanDetails(key, defaultValue, ctx, options).getValue();
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue) {
        return getBooleanDetails(key, defaultValue, null, null);
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanDetails(key, defaultValue, ctx, null);
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return this.evaluateFlag(FlagValueType.BOOLEAN, key, defaultValue, ctx, options);
    }

    @Override
    public String getStringValue(String key, String defaultValue) {
        return null;
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }

    @Override
    public Long getNumberValue(String key, Long defaultValue) {
        return null;
    }

    @Override
    public Long getNumberValue(String key, Long defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public Long getNumberValue(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue, EvaluationContext ctx) {
        return null;
    }

    @Override
    public FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return null;
    }
}

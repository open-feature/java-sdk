package dev.openfeature.sdk.testutils;

import dev.openfeature.sdk.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory provider.
 *
 * Based on flagd configuration.
 */
@Slf4j
public class InMemoryProvider implements FeatureProvider {

    @Getter
    private final String name = "InMemoryProvider";

    private Flags flags;

    private String jsonConfig;

    @Getter
    private ProviderState state = ProviderState.NOT_READY;

    @Override
    public Metadata getMetadata() {
        return new Metadata() {
            @Override
            public String getName() {
                return name;
            }
        };
    }

    public InMemoryProvider(String jsonConfig) {
        this.jsonConfig = jsonConfig;
    }

    public void initialize(EvaluationContext evaluationContext) throws Exception {
        FeatureProvider.super.initialize(evaluationContext);
        this.flags = Flags.builder().setConfigurationJson(jsonConfig).build();
        state = ProviderState.READY;
        log.info("finished initializing provider, state: {}", state);
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        if (!ProviderState.READY.equals(state)) {
            ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
            if (ProviderState.ERROR.equals(state)) {
                errorCode = ErrorCode.GENERAL;
            }
            return ProviderEvaluation.<Boolean>builder()
                .errorCode(errorCode)
                .reason(errorCode.name())
                .value(defaultValue)
                .build();
        }
        Flag flag = flags.getFlags().get(key);
        if (flag == null) {
            return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.FLAG_NOT_FOUND.name())
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
        }
        if (!(flag.getVariants().get(flag.getDefaultVariant()) instanceof Boolean)) {
            return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .variant(flag.getDefaultVariant())
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.TYPE_MISMATCH.name())
                .errorCode(ErrorCode.TYPE_MISMATCH)
                .build();
        }
        boolean value = (boolean) flag.getVariants().get(flag.getDefaultVariant());
        return ProviderEvaluation.<Boolean>builder()
            .value(value)
            .variant(flag.getDefaultVariant())
            .reason(Reason.STATIC.toString())
            .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        if (!ProviderState.READY.equals(state)) {
            ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
            if (ProviderState.ERROR.equals(state)) {
                errorCode = ErrorCode.GENERAL;
            }
            return ProviderEvaluation.<String>builder()
                .errorCode(errorCode)
                .reason(errorCode.name())
                .value(defaultValue)
                .build();
        }
        Flag flag = flags.getFlags().get(key);
        if (flag == null) {
            ProviderEvaluation<String> providerEvaluation = ProviderEvaluation.<String>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.toString())
                    .errorMessage(ErrorCode.FLAG_NOT_FOUND.name())
                    .errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .build();
            return providerEvaluation;
        }
        if (!(flag.getVariants().get(flag.getDefaultVariant()) instanceof String)) {
            return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .variant(flag.getDefaultVariant())
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.TYPE_MISMATCH.name())
                .errorCode(ErrorCode.TYPE_MISMATCH)
                .build();
        }
        String value = (String) flag.getVariants().get(flag.getDefaultVariant());
        return ProviderEvaluation.<String>builder()
            .value(value)
            .variant(flag.getDefaultVariant())
            .reason(Reason.STATIC.toString())
            .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        if (!ProviderState.READY.equals(state)) {
            ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
            if (ProviderState.ERROR.equals(state)) {
                errorCode = ErrorCode.GENERAL;
            }
            return ProviderEvaluation.<Integer>builder()
                .errorCode(errorCode)
                .reason(errorCode.name())
                .value(defaultValue)
                .build();
        }
        Flag flag = flags.getFlags().get(key);
        if (flag == null) {
            return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.FLAG_NOT_FOUND.name())
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
        }
        if (!(flag.getVariants().get(flag.getDefaultVariant()) instanceof Integer)) {
            return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .variant(flag.getDefaultVariant())
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.TYPE_MISMATCH.name())
                .errorCode(ErrorCode.TYPE_MISMATCH)
                .build();
        }
        Integer value = (Integer) flag.getVariants().get(flag.getDefaultVariant());
        return ProviderEvaluation.<Integer>builder()
            .value(value)
            .variant(flag.getDefaultVariant())
            .reason(Reason.STATIC.toString())
            .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        if (!ProviderState.READY.equals(state)) {
            ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
            if (ProviderState.ERROR.equals(state)) {
                errorCode = ErrorCode.GENERAL;
            }
            return ProviderEvaluation.<Double>builder()
                .errorCode(errorCode)
                .reason(errorCode.name())
                .value(defaultValue)
                .build();
        }
        Flag flag = flags.getFlags().get(key);
        if (flag == null) {
            return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.FLAG_NOT_FOUND.name())
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
        }
        if (!(flag.getVariants().get(flag.getDefaultVariant()) instanceof Double)) {
            return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .variant(flag.getDefaultVariant())
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.TYPE_MISMATCH.name())
                .errorCode(ErrorCode.TYPE_MISMATCH)
                .build();
        }
        Double value = (Double) flag.getVariants().get(flag.getDefaultVariant());
        return ProviderEvaluation.<Double>builder()
            .value(value)
            .variant(flag.getDefaultVariant())
            .reason(Reason.STATIC.toString())
            .build();
    }

    @SneakyThrows
    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
                                                         EvaluationContext invocationContext) {
        if (!ProviderState.READY.equals(state)) {
            ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
            if (ProviderState.ERROR.equals(state)) {
                errorCode = ErrorCode.GENERAL;
            }
            return ProviderEvaluation.<Value>builder()
                .errorCode(errorCode)
                .reason(errorCode.name())
                .value(defaultValue)
                .build();
        }
        Flag flag = flags.getFlags().get(key);
        if (flag == null) {
            return ProviderEvaluation.<Value>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.FLAG_NOT_FOUND.name())
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
        }
        Object object = flag.getVariants().get(flag.getDefaultVariant());
        Value value = ValueUtils.convert(object);
        return ProviderEvaluation.<Value>builder()
            .value(value)
            .variant(flag.getDefaultVariant())
            .reason(Reason.STATIC.toString())
            .build();
    }
}

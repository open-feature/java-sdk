package dev.openfeature.sdk.providers.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.ProviderEventDetails;
import dev.openfeature.sdk.ProviderState;
import dev.openfeature.sdk.Reason;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.exceptions.ProviderNotReadyError;
import dev.openfeature.sdk.exceptions.TypeMismatchError;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory provider.
 */
@Slf4j
public class InMemoryProvider extends EventProvider {

    @Getter
    private static final String NAME = "InMemoryProvider";

    private Map<String, Flag<?>> flags;

    @Getter
    private ProviderState state = ProviderState.NOT_READY;

    @Override
    public Metadata getMetadata() {
        return () -> NAME;
    }

    public InMemoryProvider(Map<String, Flag<?>> flags) {
        this.flags = new HashMap<>(flags);
    }

    /**
     * Initialize the provider.
     * @param evaluationContext evaluation context
     * @throws Exception on error
     */
    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        super.initialize(evaluationContext);
        state = ProviderState.READY;
        log.debug("finished initializing provider, state: {}", state);
    }

    /**
     * Updating provider flags configuration, replacing existing flags.
     * @param flags the flags to use instead of the previous flags.
     */
    public void updateFlags(Map<String, Flag<?>> flags) {
        Set<String> flagsChanged = new HashSet<>();
        flagsChanged.addAll(this.flags.keySet());
        flagsChanged.addAll(flags.keySet());
        this.flags = new HashMap<>(flags);
        ProviderEventDetails details = ProviderEventDetails.builder()
            .flagsChanged(new ArrayList<>(flagsChanged))
            .message("flags changed")
            .build();
        emitProviderConfigurationChanged(details);
    }

    /**
     * Updating provider flags configuration with adding or updating a flag.
     * @param flag the flag to update. If a flag with this key already exists, new flag replaces it.
     */
    public void updateFlag(String flagKey, Flag<?> flag) {
        this.flags.put(flagKey, flag);
        ProviderEventDetails details = ProviderEventDetails.builder()
            .flagsChanged(Arrays.asList(flagKey))
            .message("flag added/updated")
            .build();
        emitProviderConfigurationChanged(details);
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, evaluationContext, Boolean.class);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, evaluationContext, String.class);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, evaluationContext, Integer.class);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, evaluationContext, Double.class);
    }

    @SneakyThrows
    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, evaluationContext, Value.class);
    }

    private <T> ProviderEvaluation<T> getEvaluation(
            String key, EvaluationContext evaluationContext, Class<?> expectedType
    ) throws OpenFeatureError {
        if (!ProviderState.READY.equals(state)) {
            if (ProviderState.NOT_READY.equals(state)) {
                throw new ProviderNotReadyError("provider not yet initialized");
            }
            throw new GeneralError("unknown error");
        }
        Flag<?> flag = flags.get(key);
        if (flag == null) {
            throw new FlagNotFoundError("flag " + key + "not found");
        }
        T value;
        if (flag.getContextEvaluator() != null) {
            value = (T) flag.getContextEvaluator().evaluate(flag, evaluationContext);
        } else if (!expectedType.isInstance(flag.getVariants().get(flag.getDefaultVariant()))) {
            throw new TypeMismatchError("flag " + key + "is not of expected type");
        } else {
            value = (T) flag.getVariants().get(flag.getDefaultVariant());
        }
        return ProviderEvaluation.<T>builder()
            .value(value)
            .variant(flag.getDefaultVariant())
            .reason(Reason.STATIC.toString())
            .build();
    }

}

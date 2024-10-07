package dev.openfeature.sdk.providers.memory;

import dev.openfeature.sdk.*;
import dev.openfeature.sdk.exceptions.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory provider.
 */
@Slf4j
public class InMemoryProvider extends EventProvider {

    @Getter
    private static final String NAME = "InMemoryProvider";

    private final Map<String, Flag<?>> flags;

    @Getter
    private ProviderState state = ProviderState.NOT_READY;

    @Override
    public Metadata getMetadata() {
        return () -> NAME;
    }

    public InMemoryProvider(Map<String, Flag<?>> flags) {
        this.flags = new ConcurrentHashMap<>(flags);
    }

    /**
     * Initializes the provider.
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
     * Updates the provider flags configuration.
     * For existing flags, the new configurations replace the old one.
     * For new flags, they are added to the configuration.
     *
     * @param newFlags the new flag configurations
     */
    public void updateFlags(Map<String, Flag<?>> newFlags) {
        Set<String> flagsChanged = new HashSet<>(newFlags.keySet());
        this.flags.putAll(newFlags);

        ProviderEventDetails details = ProviderEventDetails.builder()
            .flagsChanged(new ArrayList<>(flagsChanged))
            .message("flags changed")
            .build();
        emitProviderConfigurationChanged(details);
    }

    /**
     * Updates a single provider flag configuration.
     * For existing flag, the new configuration replaces the old one.
     * For new flag, they are added to the configuration.
     *
     * @param newFlag the flag to update
     */
    public void updateFlag(String flagKey, Flag<?> newFlag) {
        this.flags.put(flagKey, newFlag);
        ProviderEventDetails details = ProviderEventDetails.builder()
            .flagsChanged(Collections.singletonList(flagKey))
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
            if (ProviderState.FATAL.equals(state)) {
                throw new FatalError("provider in fatal error state");
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

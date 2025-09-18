package dev.openfeature.sdk.providers.memory;

import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.ProviderEvaluation;
import dev.openfeature.api.ProviderEventDetails;
import dev.openfeature.api.ProviderMetadata;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.Reason;
import dev.openfeature.api.Value;
import dev.openfeature.api.exceptions.FatalError;
import dev.openfeature.api.exceptions.FlagNotFoundError;
import dev.openfeature.api.exceptions.GeneralError;
import dev.openfeature.api.exceptions.OpenFeatureError;
import dev.openfeature.api.exceptions.ProviderNotReadyError;
import dev.openfeature.api.exceptions.TypeMismatchError;
import dev.openfeature.sdk.EventProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory provider.
 */
public class InMemoryProvider extends EventProvider {
    private static final Logger log = LoggerFactory.getLogger(InMemoryProvider.class);
    private static final String NAME = "InMemoryProvider";

    private final Map<String, Flag<?>> flags;
    private ProviderState state = ProviderState.NOT_READY;

    public static String getName() {
        return NAME;
    }

    public ProviderState getState() {
        return state;
    }

    @Override
    public ProviderMetadata getMetadata() {
        return () -> NAME;
    }

    public InMemoryProvider(Map<String, Flag<?>> flags) {
        this.flags = new ConcurrentHashMap<>(flags);
    }

    /**
     * Initializes the provider.
     *
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

        ProviderEventDetails details = ProviderEventDetails.of("flags changed", List.copyOf(flagsChanged));
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
        ProviderEventDetails details =
                ProviderEventDetails.of("flag added/updated", Collections.singletonList(flagKey));

        emitProviderConfigurationChanged(details);
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(
            String key, Boolean defaultValue, EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Boolean.class);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(
            String key, String defaultValue, EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, String.class);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(
            String key, Integer defaultValue, EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Integer.class);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(
            String key, Double defaultValue, EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Double.class);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Value.class);
    }

    private <T> ProviderEvaluation<T> getEvaluation(
            String key, T defaultValue, EvaluationContext evaluationContext, Class<?> expectedType)
            throws OpenFeatureError {
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
            throw new FlagNotFoundError("flag " + key + " not found");
        }
        if (flag.isDisabled()) {
            return ProviderEvaluation.of(defaultValue, null, Reason.DISABLED.toString(), flag.getFlagMetadata());
        }
        T value;
        Reason reason = Reason.STATIC;
        if (flag.getContextEvaluator() != null) {
            try {
                value = (T) flag.getContextEvaluator().evaluate(flag, evaluationContext);
                reason = Reason.TARGETING_MATCH;
            } catch (Exception e) {
                value = null;
            }
            if (value == null) {
                value = (T) flag.getVariants().get(flag.getDefaultVariant());
                reason = Reason.DEFAULT;
            }
        } else if (!expectedType.isInstance(flag.getVariants().get(flag.getDefaultVariant()))) {
            throw new TypeMismatchError("flag " + key + "is not of expected type");
        } else {
            value = (T) flag.getVariants().get(flag.getDefaultVariant());
        }
        return ProviderEvaluation.of(value, flag.getDefaultVariant(), reason.toString(), flag.getFlagMetadata());
    }
}

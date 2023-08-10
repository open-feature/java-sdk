package dev.openfeature.sdk.providers.memory;

import dev.openfeature.sdk.*;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.exceptions.TypeMismatchError;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        super.initialize(evaluationContext);
        state = ProviderState.READY;
        log.info("finished initializing provider, state: {}", state);
        ProviderEventDetails details = ProviderEventDetails.builder()
            .message("provider is ready")
            .build();
        emitProviderReady(details);
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

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Boolean.class);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, String.class);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Integer.class);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Double.class);
    }

    @SneakyThrows
    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
            EvaluationContext evaluationContext) {
        return getEvaluation(key, defaultValue, evaluationContext, Value.class);
    }

    private <T> ProviderEvaluation<T> getEvaluation(
            String key, T defaultValue, EvaluationContext evaluationContext, Class<?> expectedType
    ) throws OpenFeatureError {
        if (!ProviderState.READY.equals(state)) {
            ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
            if (ProviderState.ERROR.equals(state)) {
                errorCode = ErrorCode.GENERAL;
            }
            return ProviderEvaluation.<T>builder()
                .errorCode(errorCode)
                .reason(errorCode.name())
                .value(defaultValue)
                .build();
        }
        Flag<?> flag = flags.get(key);
        if (flag == null) {
            return ProviderEvaluation.<T>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.FLAG_NOT_FOUND.name())
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .build();
        }
        T value;
        if (flag.getContextEvaluator() != null) {
            value = (T) flag.getContextEvaluator().evaluate(flag, evaluationContext);
        } else if (!expectedType.isInstance(flag.getVariants().get(flag.getDefaultVariant()))) {
            return ProviderEvaluation.<T>builder()
                .value(defaultValue)
                .variant(flag.getDefaultVariant())
                .reason(Reason.ERROR.toString())
                .errorMessage(ErrorCode.TYPE_MISMATCH.name())
                .errorCode(ErrorCode.TYPE_MISMATCH)
                .build();
        } else {
            value = (T) flag.getVariants().get(flag.getDefaultVariant());
        }
        return ProviderEvaluation.<T>builder()
            .value(value)
            .variant(flag.getDefaultVariant())
            .reason(Reason.STATIC.toString())
            .build();
    }

    private static Value objectToValue(Object object) {
        if (object instanceof Value) {
            return (Value) object;
        } else if (object == null) {
            return null;
        } else if (object instanceof String) {
            return new Value((String) object);
        } else if (object instanceof Boolean) {
            return new Value((Boolean) object);
        } else if (object instanceof Integer) {
            return new Value((Integer) object);
        } else if (object instanceof Double) {
            return new Value((Double) object);
        } else if (object instanceof Structure) {
            return new Value((Structure) object);
        } else if (object instanceof List) {
            return new Value(((List<Object>) object).stream()
                .map(InMemoryProvider::objectToValue)
                .collect(Collectors.toList()));
        } else if (object instanceof Instant) {
            return new Value((Instant) object);
        } else if (object instanceof Map) {
            return new Value(mapToStructure((Map<String, Object>) object));
        } else {
            throw new TypeMismatchError("Flag value " + object + " had unexpected type "
                    + object.getClass() + ".");
        }
    }

    /**
     * transform a map to a Structure type.
     *
     * @param map map of objects
     * @return a Structure object in the SDK format
     */
    public static Structure mapToStructure(Map<String, Object> map) {
        return new MutableStructure(
            map.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> objectToValue(e.getValue()))));
    }
}

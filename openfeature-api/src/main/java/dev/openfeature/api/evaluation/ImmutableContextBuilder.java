package dev.openfeature.api.evaluation;

import dev.openfeature.api.types.Structure;
import dev.openfeature.api.types.Value;
import java.util.Map;

/**
 * Builder class for creating instances of ImmutableContext.
 */
public interface ImmutableContextBuilder {
    ImmutableContextBuilder targetingKey(String targetingKey);

    ImmutableContextBuilder attributes(Map<String, Value> attributes);

    ImmutableContextBuilder add(String key, String value);

    ImmutableContextBuilder add(String key, Integer value);

    ImmutableContextBuilder add(String key, Long value);

    ImmutableContextBuilder add(String key, Float value);

    ImmutableContextBuilder add(String key, Double value);

    ImmutableContextBuilder add(String key, Boolean value);

    ImmutableContextBuilder add(String key, Structure value);

    ImmutableContextBuilder add(String key, Value value);

    EvaluationContext build();
}

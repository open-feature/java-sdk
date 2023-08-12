package dev.openfeature.sdk.providers.memory;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.Map;

/**
 * Flag representation.
 */
@ToString
@Builder
@Getter
public class Flag<T> {
    @Singular
    private Map<String, Object> variants;
    private String defaultVariant;
    private ContextEvaluator<T> contextEvaluator;
}

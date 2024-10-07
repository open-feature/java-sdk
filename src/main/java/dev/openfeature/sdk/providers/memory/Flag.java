package dev.openfeature.sdk.providers.memory;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

/**
 * Flag representation for the in-memory provider.
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

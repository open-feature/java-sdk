package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Singular;

@lombok.Value
@Builder
public class FlagEvaluationOptions {
    @Singular
    List<Hook> hooks;
    @Builder.Default
    Map<String, Object> hookHints = new HashMap<>();
}

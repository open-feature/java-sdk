package dev.openfeature.javasdk;

import java.util.*;

import lombok.*;

@Value
@Builder
public class FlagEvaluationOptions {
    @Singular
    List<Hook> hooks;
    @Builder.Default
    Map<String, Object> hookHints = Map.of();
}

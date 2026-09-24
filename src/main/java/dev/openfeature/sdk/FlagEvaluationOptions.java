package dev.openfeature.sdk;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Singular;

@SuppressWarnings("checkstyle:MissingJavadocType")
@lombok.Value
@Builder
public class FlagEvaluationOptions {

    public static final FlagEvaluationOptions EMPTY =
            FlagEvaluationOptions.builder().build();

    @Singular
    List<Hook> hooks;

    @Builder.Default
    Map<String, Object> hookHints = new HashMap<>();

    public static class FlagEvaluationOptionsBuilder {
        /** Sets hook hints, normalizing null to an empty map. */
        public FlagEvaluationOptionsBuilder hookHints(Map<String, Object> hookHints) {
            this.hookHints$value = hookHints != null ? hookHints : Collections.emptyMap();
            this.hookHints$set = true;
            return this;
        }
    }
}

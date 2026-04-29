package dev.openfeature.sdk;

import java.util.ArrayList;
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

    List<Hook> getHooks(FlagValueType flagValueType) {
        if (hooks == null || hooks.isEmpty()) {
            return Collections.emptyList();
        }

        var result = new ArrayList<Hook>(hooks.size());
        for (var hook : hooks) {
            if (hook.supportsFlagValueType(flagValueType)) {
                result.add(hook);
            }
        }
        return result;
    }
}

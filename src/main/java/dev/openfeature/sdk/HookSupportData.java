package dev.openfeature.sdk;

import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Encapsulates data for hook execution per flag evaluation.
 */
@Getter
class HookSupportData {

    List<Pair<Hook, HookContext>> hooks;
    EvaluationContext evaluationContext;
    Map<String, Object> hints;

    HookSupportData() {}
}

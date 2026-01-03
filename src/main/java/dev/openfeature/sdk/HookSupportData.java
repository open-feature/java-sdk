package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Map;
import lombok.Getter;

/**
 * Encapsulates data for hook execution per flag evaluation.
 */
@Getter
class HookSupportData {

    ArrayList<Pair<Hook, HookContext>> hooks;
    LayeredEvaluationContext evaluationContext;
    Map<String, Object> hints;

    HookSupportData() {}
}

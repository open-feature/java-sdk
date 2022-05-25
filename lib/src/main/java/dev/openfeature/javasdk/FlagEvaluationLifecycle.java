package dev.openfeature.javasdk;

import java.util.List;

public interface FlagEvaluationLifecycle {
    // nitpick: I think "addHooks" is clearer - it's vague if "register" will overwrite, add, etc.
    void registerHooks(Hook... hooks);
    List<Hook> getClientHooks();
}

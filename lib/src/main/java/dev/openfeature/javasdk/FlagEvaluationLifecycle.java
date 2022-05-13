package dev.openfeature.javasdk;

import java.util.List;

public interface FlagEvaluationLifecycle {
    void registerHooks(Hook... hooks);
    List<Hook> getClientHooks();
}

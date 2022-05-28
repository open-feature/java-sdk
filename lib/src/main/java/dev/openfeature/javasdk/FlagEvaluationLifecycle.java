package dev.openfeature.javasdk;

import java.util.List;

public interface FlagEvaluationLifecycle {
    void addHooks(Hook... hooks);
    List<Hook> getClientHooks();
}

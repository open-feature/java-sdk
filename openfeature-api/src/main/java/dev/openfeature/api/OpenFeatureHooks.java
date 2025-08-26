package dev.openfeature.api;

import java.util.List;

/**
 * Interface for hook management operations.
 * Provides centralized hook configuration and lifecycle management.
 */
public interface OpenFeatureHooks {
    /**
     * Adds hooks for globally, used for all evaluations.
     * Hooks are run in the order they're added in the before stage.
     * They are run in reverse order for all other stages.
     *
     * @param hooks The hooks to add.
     */
    void addHooks(Hook... hooks);

    /**
     * Fetch the hooks associated to this client.
     *
     * @return A list of {@link Hook}s.
     */
    List<Hook> getHooks();

    /**
     * Removes all hooks.
     */
    void clearHooks();
}

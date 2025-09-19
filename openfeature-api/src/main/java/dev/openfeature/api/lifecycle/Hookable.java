package dev.openfeature.api.lifecycle;

import dev.openfeature.api.Hook;
import java.util.List;

/**
 * TBD.
 */
public interface Hookable<T> {
    /**
     * Adds hooks for evaluation.
     * Hooks are run in the order they're added in the before stage. They are run in reverse order for all other stages.
     *
     * @param hooks The hook to add.
     */
    T addHooks(Hook<?>... hooks);

    /**
     * Fetch the hooks associated to this client.
     *
     * @return A list of {@link Hook}s.
     */
    List<Hook<?>> getHooks();

    /**
     * Removes all hooks.
     */
    default void clearHooks() {}
}

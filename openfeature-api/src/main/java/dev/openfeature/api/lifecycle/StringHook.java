package dev.openfeature.api.lifecycle;

import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Hook;

/**
 * An extension point which can run around flag resolution. They are intended to be used as a way to add custom logic
 * to the lifecycle of flag evaluation.
 *
 * @see Hook
 */
public interface StringHook extends Hook<String> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.STRING == flagValueType;
    }
}

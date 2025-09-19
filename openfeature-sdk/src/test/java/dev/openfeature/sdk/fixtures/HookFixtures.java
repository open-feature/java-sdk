package dev.openfeature.sdk.fixtures;

import static org.mockito.Mockito.spy;

import dev.openfeature.api.Hook;
import dev.openfeature.api.lifecycle.BooleanHook;
import dev.openfeature.api.lifecycle.DoubleHook;
import dev.openfeature.api.lifecycle.IntegerHook;
import dev.openfeature.api.lifecycle.StringHook;

public interface HookFixtures {

    default Hook<Boolean> mockBooleanHook() {
        return spy(BooleanHook.class);
    }

    default Hook<String> mockStringHook() {
        return spy(StringHook.class);
    }

    default Hook<Integer> mockIntegerHook() {
        return spy(IntegerHook.class);
    }

    default Hook<Double> mockDoubleHook() {
        return spy(DoubleHook.class);
    }

    default Hook<?> mockGenericHook() {
        return spy(Hook.class);
    }
}

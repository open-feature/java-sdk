package dev.openfeature.sdk.fixtures;

import dev.openfeature.sdk.*;

import static org.mockito.Mockito.spy;

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

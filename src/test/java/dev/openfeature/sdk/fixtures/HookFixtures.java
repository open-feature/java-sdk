package dev.openfeature.sdk.fixtures;

import static org.mockito.Mockito.spy;

import dev.openfeature.sdk.BooleanHook;
import dev.openfeature.sdk.DoubleHook;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.IntegerHook;
import dev.openfeature.sdk.ObjectHook;
import dev.openfeature.sdk.StringHook;

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

    default Hook<Object> mockObjectHook() {
        return spy(ObjectHook.class);
    }

    default Hook<?> mockGenericHook() {
        return spy(Hook.class);
    }
}

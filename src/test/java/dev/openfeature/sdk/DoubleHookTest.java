package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.fixtures.HookFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoubleHookTest implements HookFixtures {

    private Hook<Double> hook;

    @BeforeEach
    void setupTest() {
        hook = mockDoubleHook();
    }

    @Test
    void verifyFlagValueTypeIsSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.DOUBLE);

        assertThat(hookSupported).isTrue();
    }

    @Test
    void verifyFlagValueTypeIsNotSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.STRING);

        assertThat(hookSupported).isFalse();
    }
}

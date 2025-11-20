package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.fixtures.HookFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegerHookTest implements HookFixtures {

    private Hook<Integer> hook;

    @BeforeEach
    void setupTest() {
        hook = mockIntegerHook();
    }

    @Test
    void verifyFlagValueTypeIsSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.INTEGER);

        assertThat(hookSupported).isTrue();
    }

    @Test
    void verifyFlagValueTypeIsNotSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.STRING);

        assertThat(hookSupported).isFalse();
    }
}

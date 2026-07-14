package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.fixtures.HookFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LongHookTest implements HookFixtures {

    private Hook<Long> hook;

    @BeforeEach
    void setupTest() {
        hook = mockLongHook();
    }

    @Test
    void verifyFlagValueTypeIsSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.LONG);

        assertThat(hookSupported).isTrue();
    }

    @Test
    void verifyFlagValueTypeIsNotSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.STRING);

        assertThat(hookSupported).isFalse();
    }

    @Test
    void verifyIntegerNotSupportedByLongHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.INTEGER);

        assertThat(hookSupported).isFalse();
    }
}

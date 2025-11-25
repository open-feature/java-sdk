package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.fixtures.HookFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringHookTest implements HookFixtures {

    private Hook<String> hook;

    @BeforeEach
    void setupTest() {
        hook = mockStringHook();
    }

    @Test
    void verifyFlagValueTypeIsSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.STRING);

        assertThat(hookSupported).isTrue();
    }

    @Test
    void verifyFlagValueTypeIsNotSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.INTEGER);

        assertThat(hookSupported).isFalse();
    }
}

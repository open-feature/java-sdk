package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.fixtures.HookFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectHookTest implements HookFixtures {

    private Hook<Object> hook;

    @BeforeEach
    void setupTest() {
        hook = mockObjectHook();
    }

    @Test
    void verifyFlagValueTypeIsSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.OBJECT);

        assertThat(hookSupported).isTrue();
    }

    @Test
    void verifyFlagValueTypeIsNotSupportedByHook() {
        boolean hookSupported = hook.supportsFlagValueType(FlagValueType.INTEGER);

        assertThat(hookSupported).isFalse();
    }
}

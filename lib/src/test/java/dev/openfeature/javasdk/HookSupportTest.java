package dev.openfeature.javasdk;

import java.util.*;

import dev.openfeature.javasdk.fixtures.HookFixtures;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HookSupportTest implements HookFixtures {

    @Test
    @DisplayName("should merge EvaluationContexts on before hooks correctly")
    void shouldMergeEvaluationContextsOnBeforeHooksCorrectly() {
        var baseContext = new EvaluationContext();
        baseContext.addStringAttribute("baseKey", "baseValue");
        var hookContext = new HookContext<>("flagKey", FlagValueType.STRING, "defaultValue", baseContext, () -> "client", () -> "provider");
        var hook1 = mockStringHook();
        var hook2 = mockStringHook();
        when(hook1.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("bla", "blubber")));
        when(hook2.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("foo", "bar")));
        var hookSupport = new HookSupport();

        var result = hookSupport.beforeHooks(FlagValueType.STRING, hookContext, List.of(hook1, hook2), Map.of());

        assertThat(result.getStringAttribute("bla")).isEqualTo("blubber");
        assertThat(result.getStringAttribute("foo")).isEqualTo("bar");
        assertThat(result.getStringAttribute("baseKey")).isEqualTo("baseValue");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should always call generic hook")
    void shouldAlwaysCallGenericHook(FlagValueType flagValueType) {
        var genericHook = mockGenericHook();
        var hookSupport = new HookSupport();
        var baseContext = new EvaluationContext();
        var hookContext = new HookContext<>("flagKey", flagValueType, createDefaultValue(flagValueType), baseContext, () -> "client", () -> "provider");

        hookSupport.beforeHooks(flagValueType, hookContext, List.of(genericHook), Map.of());
        hookSupport.afterHooks(flagValueType, hookContext, FlagEvaluationDetails.builder().build(), List.of(genericHook), Map.of());
        hookSupport.afterAllHooks(flagValueType, hookContext, List.of(genericHook), Map.of());
        hookSupport.errorHooks(flagValueType, hookContext, new IllegalStateException("All fine, just a test"), List.of(genericHook), Map.of());

        verify(genericHook).before(any(), any());
        verify(genericHook).after(any(), any(), any());
        verify(genericHook).finallyAfter(any(), any());
        verify(genericHook).error(any(), any(), any());
    }

    private Object createDefaultValue(FlagValueType flagValueType) {
        switch (flagValueType) {
            case INTEGER:
                return 1;
            case BOOLEAN:
                return true;
            case STRING:
                return "defaultValue";
            case OBJECT:
                return "object";
            default:
                throw new IllegalArgumentException();
        }
    }

    private EvaluationContext evaluationContextWithValue(String key, String value) {
        var result = new EvaluationContext();
        result.addStringAttribute(key, value);
        return result;
    }

}

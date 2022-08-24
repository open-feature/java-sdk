package dev.openfeature.javasdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import dev.openfeature.javasdk.fixtures.HookFixtures;

class HookSupportTest implements HookFixtures {

    @Test
    @DisplayName("should merge EvaluationContexts on before hooks correctly")
    void shouldMergeEvaluationContextsOnBeforeHooksCorrectly() {
        EvaluationContext baseContext = new EvaluationContext();
        baseContext.add("baseKey", "baseValue");
        HookContext<String> hookContext = new HookContext<>("flagKey", FlagValueType.STRING, "defaultValue", baseContext, () -> "client", () -> "provider");
        Hook<String> hook1 = mockStringHook();
        Hook<String> hook2 = mockStringHook();
        when(hook1.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("bla", "blubber")));
        when(hook2.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("foo", "bar")));
        HookSupport hookSupport = new HookSupport();

        EvaluationContext result = hookSupport.beforeHooks(FlagValueType.STRING, hookContext, Arrays.asList(hook1, hook2), Collections.emptyMap());

        assertThat(result.getStringAttribute("bla")).isEqualTo("blubber");
        assertThat(result.getStringAttribute("foo")).isEqualTo("bar");
        assertThat(result.getStringAttribute("baseKey")).isEqualTo("baseValue");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should always call generic hook")
    void shouldAlwaysCallGenericHook(FlagValueType flagValueType) {
        Hook<?> genericHook = mockGenericHook();
        HookSupport hookSupport = new HookSupport();
        EvaluationContext baseContext = new EvaluationContext();
        IllegalStateException expectedException = new IllegalStateException("All fine, just a test");
        HookContext<Object> hookContext = new HookContext<>("flagKey", flagValueType, createDefaultValue(flagValueType), baseContext, () -> "client", () -> "provider");

        hookSupport.beforeHooks(flagValueType, hookContext, Collections.singletonList(genericHook), Collections.emptyMap());
        hookSupport.afterHooks(flagValueType, hookContext, FlagEvaluationDetails.builder().build(), Collections.singletonList(genericHook), Collections.emptyMap());
        hookSupport.afterAllHooks(flagValueType, hookContext, Collections.singletonList(genericHook), Collections.emptyMap());
        hookSupport.errorHooks(flagValueType, hookContext, expectedException, Collections.singletonList(genericHook), Collections.emptyMap());

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
            case DOUBLE:
                return "double";
            default:
                throw new IllegalArgumentException();
        }
    }

    private EvaluationContext evaluationContextWithValue(String key, String value) {
        EvaluationContext result = new EvaluationContext();
        result.add(key, value);
        return result;
    }

}

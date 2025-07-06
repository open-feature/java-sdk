package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.openfeature.sdk.fixtures.HookFixtures;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HookSupportTest implements HookFixtures {
    @Test
    @DisplayName("should merge EvaluationContexts on before hooks correctly")
    void shouldMergeEvaluationContextsOnBeforeHooksCorrectly() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("baseKey", new Value("baseValue"));
        EvaluationContext baseContext = new ImmutableContext(attributes);
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("flagKey")
                .type(FlagValueType.STRING)
                .defaultValue("defaultValue")
                .ctx(baseContext)
                .clientMetadata(() -> "client")
                .providerMetadata(() -> "provider")
                .build();
        Hook<String> hook1 = mockStringHook();
        Hook<String> hook2 = mockStringHook();
        when(hook1.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("bla", "blubber")));
        when(hook2.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("foo", "bar")));
        HookSupport hookSupport = new HookSupport();

        EvaluationContext result = hookSupport.beforeHooks(
                FlagValueType.STRING, hookContext, Arrays.asList(hook1, hook2), Collections.emptyMap());

        assertThat(result.getValue("bla").asString()).isEqualTo("blubber");
        assertThat(result.getValue("foo").asString()).isEqualTo("bar");
        assertThat(result.getValue("baseKey").asString()).isEqualTo("baseValue");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should always call generic hook")
    void shouldAlwaysCallGenericHook(FlagValueType flagValueType) {
        Hook<?> genericHook = mockGenericHook();
        HookSupport hookSupport = new HookSupport();
        EvaluationContext baseContext = new ImmutableContext();
        IllegalStateException expectedException = new IllegalStateException("All fine, just a test");
        HookContext<Object> hookContext = HookContext.<Object>builder()
                .flagKey("flagKey")
                .type(flagValueType)
                .defaultValue(createDefaultValue(flagValueType))
                .ctx(baseContext)
                .clientMetadata(() -> "client")
                .providerMetadata(() -> "provider")
                .build();

        hookSupport.beforeHooks(
                flagValueType, hookContext, Collections.singletonList(genericHook), Collections.emptyMap());
        hookSupport.afterHooks(
                flagValueType,
                hookContext,
                FlagEvaluationDetails.builder().build(),
                Collections.singletonList(genericHook),
                Collections.emptyMap());
        hookSupport.afterAllHooks(
                flagValueType,
                hookContext,
                FlagEvaluationDetails.builder().build(),
                Collections.singletonList(genericHook),
                Collections.emptyMap());
        hookSupport.errorHooks(
                flagValueType,
                hookContext,
                expectedException,
                Collections.singletonList(genericHook),
                Collections.emptyMap());

        verify(genericHook).before(any(), any());
        verify(genericHook).after(any(), any(), any());
        verify(genericHook).finallyAfter(any(), any(), any());
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
        Map<String, Value> attributes = new HashMap<>();
        attributes.put(key, new Value(value));
        EvaluationContext baseContext = new ImmutableContext(attributes);
        return baseContext;
    }

    private static class TestHook implements Hook<String> {
        boolean beforeCalled = false;
        boolean afterCalled = false;
        boolean errorCalled = false;
        boolean finallyCalled = false;

        @Override
        public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
            beforeCalled = true;
            return Optional.empty();
        }

        @Override
        public void after(HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
            afterCalled = true;
        }

        @Override
        public void error(HookContext<String> ctx, Exception error, Map<String, Object> hints) {
            errorCalled = true;
        }

        @Override
        public void finallyAfter(HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
            finallyCalled = true;
        }
    }
}

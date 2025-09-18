package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.openfeature.api.*;
import dev.openfeature.sdk.fixtures.HookFixtures;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        EvaluationContext baseContext = EvaluationContext.immutableOf(attributes);
        FlagValueType valueType = FlagValueType.STRING;
        HookContext<String> hookContext = new HookContextWithoutData<>(
                "flagKey", valueType, "defaultValue", () -> "client", () -> "provider", baseContext);
        Hook<String> hook1 = mockStringHook();
        Hook<String> hook2 = mockStringHook();
        when(hook1.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("bla", "blubber")));
        when(hook2.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("foo", "bar")));
        HookSupport hookSupport = new HookSupport();

        EvaluationContext result = hookSupport.beforeHooks(
                valueType,
                hookContext,
                hookSupport.getHookDataPairs(Arrays.asList(hook1, hook2), valueType),
                Collections.emptyMap());

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
        var hookDataPairs = hookSupport.getHookDataPairs(Collections.singletonList(genericHook), flagValueType);
        EvaluationContext baseContext = EvaluationContext.EMPTY;
        IllegalStateException expectedException = new IllegalStateException("All fine, just a test");
        HookContext<Object> hookContext = new HookContextWithoutData<>(
                "flagKey",
                flagValueType,
                createDefaultValue(flagValueType),
                () -> "client",
                () -> "provider",
                baseContext);

        hookSupport.beforeHooks(flagValueType, hookContext, hookDataPairs, Collections.emptyMap());
        hookSupport.afterHooks(
                flagValueType, hookContext, FlagEvaluationDetails.EMPTY, hookDataPairs, Collections.emptyMap());
        hookSupport.afterAllHooks(
                flagValueType, hookContext, FlagEvaluationDetails.EMPTY, hookDataPairs, Collections.emptyMap());
        hookSupport.errorHooks(flagValueType, hookContext, expectedException, hookDataPairs, Collections.emptyMap());

        verify(genericHook).before(any(), any());
        verify(genericHook).after(any(), any(), any());
        verify(genericHook).finallyAfter(any(), any(), any());
        verify(genericHook).error(any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should allow hooks to store and retrieve data across stages")
    void shouldPassDataAcrossStages(FlagValueType flagValueType) {
        HookSupport hookSupport = new HookSupport();
        HookContext<Object> hookContext = getObjectHookContext(flagValueType);

        TestHookWithData testHook = new TestHookWithData("test-key", "value");
        var pairs = hookSupport.getHookDataPairs(List.of(testHook), flagValueType);

        callAllHooks(flagValueType, hookSupport, hookContext, testHook);

        assertHookData(testHook, "value");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should isolate data between different hook instances")
    void shouldIsolateDataBetweenHooks(FlagValueType flagValueType) {
        HookSupport hookSupport = new HookSupport();
        HookContext<Object> hookContext = getObjectHookContext(flagValueType);

        TestHookWithData testHook1 = new TestHookWithData("test-key", "value-1");
        TestHookWithData testHook2 = new TestHookWithData("test-key", "value-2");
        var pairs = hookSupport.getHookDataPairs(List.of(testHook1, testHook2), flagValueType);

        callAllHooks(flagValueType, hookSupport, hookContext, pairs);

        assertHookData(testHook1, "value-1");
        assertHookData(testHook2, "value-2");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should isolate data between the same hook instances")
    void shouldIsolateDataBetweenSameHooks(FlagValueType flagValueType) {

        HookSupport hookSupport = new HookSupport();
        HookContext<Object> hookContext = getObjectHookContext(flagValueType);

        TestHookWithData testHook = new TestHookWithData("test-key", "value-1");

        // run hooks first time
        callAllHooks(flagValueType, hookSupport, hookContext, testHook);
        assertHookData(testHook, "value-1");

        // re-run with different value, will throw if HookData contains already data
        testHook.value = "value-2";
        callAllHooks(flagValueType, hookSupport, hookContext, testHook);

        assertHookData(testHook, "value-2");
    }

    private HookContext<Object> getObjectHookContext(FlagValueType flagValueType) {
        EvaluationContext baseContext = EvaluationContext.EMPTY;

        return new HookContextWithoutData<>(
                "flagKeyf",
                flagValueType,
                createDefaultValue(flagValueType),
                () -> "client",
                () -> "provider",
                baseContext);
    }

    private static void assertHookData(TestHookWithData testHook1, String expected) {
        assertThat(testHook1.onBeforeValue).isEqualTo(expected);
        assertThat(testHook1.onFinallyAfterValue).isEqualTo(expected);
        assertThat(testHook1.onAfterValue).isEqualTo(expected);
        assertThat(testHook1.onErrorValue).isEqualTo(expected);
    }

    private static void callAllHooks(
            FlagValueType flagValueType,
            HookSupport hookSupport,
            HookContext<Object> hookContext,
            TestHookWithData testHook) {
        var pairs = hookSupport.getHookDataPairs(List.of(testHook), flagValueType);
        callAllHooks(flagValueType, hookSupport, hookContext, pairs);
    }

    private static void callAllHooks(
            FlagValueType flagValueType,
            HookSupport hookSupport,
            HookContext<Object> hookContext,
            List<Pair<Hook, HookData>> pairs) {
        hookSupport.beforeHooks(flagValueType, hookContext, pairs, Collections.emptyMap());
        hookSupport.afterHooks(flagValueType, hookContext, FlagEvaluationDetails.EMPTY, pairs, Collections.emptyMap());
        hookSupport.errorHooks(flagValueType, hookContext, new Exception(), pairs, Collections.emptyMap());
        hookSupport.afterAllHooks(
                flagValueType, hookContext, FlagEvaluationDetails.EMPTY, pairs, Collections.emptyMap());
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
        EvaluationContext baseContext = EvaluationContext.immutableOf(attributes);
        return baseContext;
    }

    private class TestHookWithData implements Hook {

        private final String key;
        Object value;

        Object onBeforeValue;
        Object onAfterValue;
        Object onErrorValue;
        Object onFinallyAfterValue;

        TestHookWithData(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
            var storedValue = ctx.getHookData().get(key);
            if (storedValue != null) {
                throw new Error("Hook data isolation violated! Data is already set.");
            }
            ctx.getHookData().set(key, value);
            onBeforeValue = ctx.getHookData().get(key);
            return Optional.empty();
        }

        @Override
        public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
            onAfterValue = ctx.getHookData().get(key);
        }

        @Override
        public void error(HookContext ctx, Exception error, Map hints) {
            onErrorValue = ctx.getHookData().get(key);
        }

        @Override
        public void finallyAfter(HookContext ctx, FlagEvaluationDetails details, Map hints) {
            onFinallyAfterValue = ctx.getHookData().get(key);
        }
    }
}

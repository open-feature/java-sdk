package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

class HookExecutorTest implements HookFixtures {
    @Test
    @DisplayName("should merge EvaluationContexts on before hooks correctly")
    void shouldMergeEvaluationContextsOnBeforeHooksCorrectly() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("baseKey", new Value("baseValue"));
        EvaluationContext baseContext = new ImmutableContext(attributes);

        Hook<String> hook1 = mockStringHook();
        Hook<String> hook2 = mockStringHook();
        when(hook1.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("bla", "blubber")));
        when(hook2.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("foo", "bar")));

        HookExecutor executor = HookExecutor.create(
                Arrays.asList(hook1, hook2), getBaseHookContextForType(FlagValueType.STRING), baseContext, Collections.emptyMap());

        executor.executeBeforeHooks();

        EvaluationContext result = executor.getEvaluationContext();

        assertThat(result.getValue("bla").asString()).isEqualTo("blubber");
        assertThat(result.getValue("foo").asString()).isEqualTo("bar");
        assertThat(result.getValue("baseKey").asString()).isEqualTo("baseValue");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should always call generic hook")
    void shouldAlwaysCallGenericHook(FlagValueType flagValueType) {
        Hook<?> genericHook = mockGenericHook();

        HookExecutor hookExecutor = HookExecutor.create(List.of(genericHook), getBaseHookContextForType(flagValueType), ImmutableContext.EMPTY, Collections.emptyMap());

        callAllHooks(hookExecutor);

        verify(genericHook).before(any(), any());
        verify(genericHook).after(any(), any(), any());
        verify(genericHook).finallyAfter(any(), any(), any());
        verify(genericHook).error(any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should allow hooks to store and retrieve data across stages")
    void shouldPassDataAcrossStages(FlagValueType flagValueType) {
        var testHook = new HookDataHook();
        HookExecutor hookExecutor = HookExecutor.create(List.of(testHook), getBaseHookContextForType(flagValueType), ImmutableContext.EMPTY, Collections.emptyMap());

        hookExecutor.executeBeforeHooks();
        assertHookData(testHook, "before");

        hookExecutor.executeAfterHooks(FlagEvaluationDetails.builder().build());
        assertHookData(testHook, "before", "after");

        hookExecutor.executeAfterAllHooks(FlagEvaluationDetails.builder().build());
        assertHookData(testHook, "before", "after", "finallyAfter");

        hookExecutor.executeErrorHooks(mock(Exception.class));
        assertHookData(testHook, "before", "after", "finallyAfter", "error");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should isolate data between different hook instances")
    void shouldIsolateDataBetweenHooks(FlagValueType flagValueType) {
        var testHook1 = new HookDataHook(1);
        var testHook2 = new HookDataHook(2);

        HookExecutor hookExecutor = HookExecutor.create(List.of(testHook1, testHook2), getBaseHookContextForType(flagValueType), ImmutableContext.EMPTY, Collections.emptyMap());

        callAllHooks(hookExecutor);

        assertHookData(testHook1, 1, "before", "after", "finallyAfter", "error");
        assertHookData(testHook2, 2, "before", "after", "finallyAfter", "error");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should isolate data between the same hook executions")
    void shouldIsolateDataBetweenSameHookExecutions(FlagValueType flagValueType) {
        TestHookWithData testHook = new TestHookWithData("test-key", "value-1");

        HookExecutor hookExecutor1 = HookExecutor.create(List.of(testHook), getBaseHookContextForType(flagValueType), ImmutableContext.EMPTY, Collections.emptyMap());
        HookExecutor hookExecutor2 = HookExecutor.create(List.of(testHook), getBaseHookContextForType(flagValueType), ImmutableContext.EMPTY, Collections.emptyMap());

        // run hooks first time
        callAllHooks(hookExecutor1);
        assertHookData(testHook, "value-1");

        // re-run with different value, will throw if HookData contains already data
        testHook.value = "value-2";
        callAllHooks(hookExecutor2);
        assertHookData(testHook, "value-2");
    }

    private static void callAllHooks(HookExecutor hookExecutor) {
        hookExecutor.executeBeforeHooks();
        hookExecutor.executeAfterHooks(FlagEvaluationDetails.builder().build());
        hookExecutor.executeAfterAllHooks(FlagEvaluationDetails.builder().build());
        hookExecutor.executeErrorHooks(mock(Exception.class));
    }

    private static void assertHookData(TestHookWithData testHook1, String expected) {
        assertThat(testHook1.onBeforeValue).isEqualTo(expected);
        assertThat(testHook1.onFinallyAfterValue).isEqualTo(expected);
        assertThat(testHook1.onAfterValue).isEqualTo(expected);
        assertThat(testHook1.onErrorValue).isEqualTo(expected);
    }

    private static void assertHookData(HookDataHook testHook, String ... expectedKeys) {
        for (String expectedKey : expectedKeys) {
            assertThat(testHook.hookData.get(expectedKey))
                    .withFailMessage("Expected key %s not present in hook data", expectedKey)
                    .isNotNull();
        }
    }

    private static void assertHookData(HookDataHook testHook, Object expectedValue, String ... expectedKeys) {
        for (String expectedKey : expectedKeys) {
            assertThat(testHook.hookData.get(expectedKey))
                    .withFailMessage("Expected key '%s' not present in hook data", expectedKey)
                    .isNotNull();
            assertThat(testHook.hookData.get(expectedKey))
                    .withFailMessage("Expected key '%s' not containing expected value. Expected '%s' but found '%s'",
                            expectedKey, expectedValue, testHook.hookData.get(expectedKey))
                    .isEqualTo(expectedValue);
        }
    }

    private SharedHookContext getBaseHookContextForType(FlagValueType flagValueType) {
        return new SharedHookContext<>(
                "flagKey",
                flagValueType,
                () -> "client",
                () -> "provider",
                createDefaultValue(flagValueType));
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
        return new ImmutableContext(attributes);
    }

    private static class HookDataHook implements Hook {
        private final Object value;
        HookData hookData = null;

        public HookDataHook(Object value) {
            this.value = value;
        }

        public HookDataHook() {
            this("test");
        }

        @Override
        public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
            ctx.getHookData().set("before", value);
            hookData = ctx.getHookData();
            return Optional.empty();
        }

        @Override
        public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
            ctx.getHookData().set("after", value);
            hookData = ctx.getHookData();
        }

        @Override
        public void error(HookContext ctx, Exception error, Map hints) {
            ctx.getHookData().set("error", value);
            hookData = ctx.getHookData();
        }

        @Override
        public void finallyAfter(HookContext ctx, FlagEvaluationDetails details, Map hints) {
            ctx.getHookData().set("finallyAfter", value);
            hookData = ctx.getHookData();
        }
    }

    private class TestHookWithData implements Hook {

        private final String key;
        Object value;

        Object onBeforeValue;
        Object onAfterValue;
        Object onErrorValue;
        Object onFinallyAfterValue;

        TestHookWithData(Object value) {
            this("test", value);
        }

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

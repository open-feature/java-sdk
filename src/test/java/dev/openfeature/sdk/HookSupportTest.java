package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.openfeature.sdk.fixtures.HookFixtures;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HookSupportTest implements HookFixtures {

    private static final HookSupport hookSupport = new HookSupport();

    @Test
    @DisplayName("should merge EvaluationContexts on before hooks correctly")
    void shouldMergeEvaluationContextsOnBeforeHooksCorrectly() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("baseKey", new Value("baseValue"));
        EvaluationContext baseEvalContext = new ImmutableContext(attributes);

        Hook<String> hook1 = mockStringHook();
        Hook<String> hook2 = mockStringHook();
        when(hook1.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("bla", "blubber")));
        when(hook2.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("foo", "bar")));

        var layered = new LayeredEvaluationContext(baseEvalContext, null, null, null);
        var sharedContext = getBaseHookContextForType(FlagValueType.STRING);
        var hookSupportData = new HookSupportData();
        hookSupportData.evaluationContext = layered;
        hookSupport.setHooks(hookSupportData, Arrays.asList(hook1, hook2), FlagValueType.STRING);
        hookSupport.setHookContexts(hookSupportData, sharedContext, layered);

        hookSupport.executeBeforeHooks(hookSupportData);

        EvaluationContext result = hookSupportData.getEvaluationContext();

        assertThat(result.getValue("bla").asString()).isEqualTo("blubber");
        assertThat(result.getValue("foo").asString()).isEqualTo("bar");
        assertThat(result.getValue("baseKey").asString()).isEqualTo("baseValue");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should always call generic hook")
    void shouldAlwaysCallGenericHook(FlagValueType flagValueType) {
        Hook<?> genericHook = mockGenericHook();

        var hookSupportData = new HookSupportData();
        hookSupport.setHooks(hookSupportData, List.of(genericHook), flagValueType);

        callAllHooks(hookSupportData);

        verify(genericHook).before(any(), any());
        verify(genericHook).after(any(), any(), any());
        verify(genericHook).finallyAfter(any(), any(), any());
        verify(genericHook).error(any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should allow hooks to store and retrieve data across stages")
    void shouldPassDataAcrossStages(FlagValueType flagValueType) {
        var testHook = new TestHookWithData();
        var hookSupportData = new HookSupportData();
        hookSupport.setHooks(hookSupportData, List.of(testHook), flagValueType);
        hookSupport.setHookContexts(
                hookSupportData,
                getBaseHookContextForType(flagValueType),
                new LayeredEvaluationContext(null, null, null, null));

        hookSupport.executeBeforeHooks(hookSupportData);
        assertHookData(testHook, "before");

        hookSupport.executeAfterHooks(
                hookSupportData, FlagEvaluationDetails.builder().build());
        assertHookData(testHook, "before", "after");

        hookSupport.executeAfterAllHooks(
                hookSupportData, FlagEvaluationDetails.builder().build());
        assertHookData(testHook, "before", "after", "finallyAfter");

        hookSupport.executeErrorHooks(hookSupportData, mock(Exception.class));
        assertHookData(testHook, "before", "after", "finallyAfter", "error");
    }

    @ParameterizedTest
    @EnumSource(value = FlagValueType.class)
    @DisplayName("should isolate data between different hook instances")
    void shouldIsolateDataBetweenHooks(FlagValueType flagValueType) {
        var testHook1 = new TestHookWithData(1);
        var testHook2 = new TestHookWithData(2);

        var hookSupportData = new HookSupportData();
        hookSupport.setHooks(hookSupportData, List.of(testHook1, testHook2), flagValueType);
        hookSupport.setHookContexts(
                hookSupportData,
                getBaseHookContextForType(flagValueType),
                new LayeredEvaluationContext(null, null, null, null));

        callAllHooks(hookSupportData);

        assertHookData(testHook1, 1, "before", "after", "finallyAfter", "error");
        assertHookData(testHook2, 2, "before", "after", "finallyAfter", "error");
    }

    @Test
    void hookThatReturnsTheGivenContext_doesNotResultInAStackOverflow() {
        var hookSupportData = new HookSupportData();
        var recursiveHook = new Hook() {
            @Override
            public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
                return Optional.of(ctx.getCtx());
            }
        };
        var emptyHook = new Hook() {
            @Override
            public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
                return Optional.of(ImmutableContext.EMPTY);
            }
        };
        var layeredEvaluationContext =
                new LayeredEvaluationContext(evaluationContextWithValue("key", "value"), null, null, null);
        hookSupportData.evaluationContext = layeredEvaluationContext;
        hookSupport.setHooks(hookSupportData, List.of(recursiveHook, emptyHook), FlagValueType.STRING);
        hookSupport.setHookContexts(
                hookSupportData, getBaseHookContextForType(FlagValueType.STRING), layeredEvaluationContext);

        callAllHooks(hookSupportData);

        assertThatNoException().isThrownBy(layeredEvaluationContext::asObjectMap);
    }

    private static void callAllHooks(HookSupportData hookSupportData) {
        hookSupport.executeBeforeHooks(hookSupportData);
        hookSupport.executeAfterHooks(
                hookSupportData, FlagEvaluationDetails.builder().build());
        hookSupport.executeAfterAllHooks(
                hookSupportData, FlagEvaluationDetails.builder().build());
        hookSupport.executeErrorHooks(hookSupportData, mock(Exception.class));
    }

    private static void assertHookData(TestHookWithData testHook, String... expectedKeys) {
        for (String expectedKey : expectedKeys) {
            assertThat(testHook.hookData.get(expectedKey))
                    .withFailMessage("Expected key %s not present in hook data", expectedKey)
                    .isNotNull();
        }
    }

    private static void assertHookData(TestHookWithData testHook, Object expectedValue, String... expectedKeys) {
        for (String expectedKey : expectedKeys) {
            assertThat(testHook.hookData.get(expectedKey))
                    .withFailMessage("Expected key '%s' not present in hook data", expectedKey)
                    .isNotNull();
            assertThat(testHook.hookData.get(expectedKey))
                    .withFailMessage(
                            "Expected key '%s' not containing expected value. Expected '%s' but found '%s'",
                            expectedKey, expectedValue, testHook.hookData.get(expectedKey))
                    .isEqualTo(expectedValue);
        }
    }

    private SharedHookContext getBaseHookContextForType(FlagValueType flagValueType) {
        return new SharedHookContext<>(
                "flagKey", flagValueType, () -> "client", () -> "provider", createDefaultValue(flagValueType));
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
}

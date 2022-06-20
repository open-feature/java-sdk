package dev.openfeature.javasdk;

import java.util.*;

import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HookSupportTest {

    @Test
    @DisplayName("should merge EvaluationContexts on before hooks correctly")
    void shouldMergeEvaluationContextsOnBeforeHooksCorrectly() {
        var baseContext = new EvaluationContext();
        baseContext.addStringAttribute("baseKey", "baseValue");
        var hookContext = new HookContext<>("flagKey", FlagValueType.STRING, "defaultValue", baseContext, () -> "client", () -> "provider");
        var hook1 = stringHookMock();
        var hook2 = stringHookMock();
        when(hook1.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("bla", "blubber")));
        when(hook2.before(any(), any())).thenReturn(Optional.of(evaluationContextWithValue("foo", "bar")));

        var hookSupport = new HookSupport(LoggerFactory.getLogger("test"));

        var result = hookSupport.beforeHooks(FlagValueType.STRING, hookContext, List.of(hook1, hook2), Map.of());

        assertThat(result.getStringAttribute("bla")).isEqualTo("blubber");
        assertThat(result.getStringAttribute("foo")).isEqualTo("bar");
        assertThat(result.getStringAttribute("baseKey")).isEqualTo("baseValue");
    }

    private EvaluationContext evaluationContextWithValue(String key, String value) {
        var result = new EvaluationContext();
        result.addStringAttribute(key, value);
        return result;
    }

    private StringHook stringHookMock() {
        return spy(StringHook.class);
    }

}

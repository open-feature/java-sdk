package dev.openfeature.javasdk;

import java.util.*;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.*;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OpenFeatureClientTest {

    private static final TestLogger TEST_LOGGER = TestLoggerFactory.getTestLogger(OpenFeatureClient.class);

    @Test
    @DisplayName("should not throw exception if hook has different type argument than hookContext")
    void shouldNotThrowExceptionIfHookHasDifferentTypeArgumentThanHookContext() {
        TEST_LOGGER.clear();
        var api = mock(OpenFeatureAPI.class);
        when(api.getProvider()).thenReturn(new DoSomethingProvider());
        when(api.getApiHooks()).thenReturn(List.of(new BooleanHook(), new StringHook()));

        var client = new OpenFeatureClient(api, "name", "version");

        var actual = client.getBooleanDetails("feature key", Boolean.FALSE);

        assertThat(actual.getValue()).isFalse();
        assertThat(TEST_LOGGER.getLoggingEvents()).filteredOn(event -> event.getLevel().equals(Level.ERROR)).isEmpty();
    }

    private static class BooleanHook implements Hook<Boolean> {
        @Override
        public FlagValueType supportsFlagValueType() {
            return FlagValueType.BOOLEAN;
        }
    }

    private static class StringHook implements Hook<String> {
        @Override
        public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
            var result = new EvaluationContext();
            result.addStringAttribute("lowercase", ctx.getDefaultValue().toLowerCase());
            return Optional.of(result);
        }

        @Override
        public void after(HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
            Hook.super.after(ctx, details, hints);
        }

        @Override
        public void error(HookContext<String> ctx, Exception error, Map<String, Object> hints) {
            Hook.super.error(ctx, error, hints);
        }

        @Override
        public void finallyAfter(HookContext<String> ctx, Map<String, Object> hints) {
            Hook.super.finallyAfter(ctx, hints);
        }

        @Override
        public FlagValueType supportsFlagValueType() {
            return FlagValueType.STRING;
        }
    }
}

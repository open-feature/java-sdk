package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.openfeature.api.AbstractEventProvider;
import dev.openfeature.api.Hook;
import dev.openfeature.api.Provider;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.events.EventDetails;
import dev.openfeature.api.events.EventProvider;
import dev.openfeature.api.events.ProviderEventDetails;
import dev.openfeature.api.internal.TriConsumer;
import dev.openfeature.api.internal.noop.NoOpProvider;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;
import dev.openfeature.sdk.testutils.TestStackedEmitCallsProvider;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class EventProviderTest {

    private static final int TIMEOUT = 300;

    private TestEventProvider eventProvider;

    @BeforeEach
    void setup() throws Exception {
        eventProvider = new TestEventProvider();
        eventProvider.initialize(null);
        eventProvider.setEventEmitter(new DefaultEventEmitter(eventProvider, null));
    }

    @AfterAll
    public static void resetDefaultProvider() {
        new DefaultOpenFeatureAPI().setProviderAndWait(new NoOpProvider());
    }

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("should run attached onEmit with emitters")
    void emitsEventsWhenAttached() {
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mockOnEmit();
        eventProvider.attach(onEmit);

        EventDetails details = EventDetails.EMPTY;
        eventProvider.emit(ProviderEvent.PROVIDER_READY, details);
        eventProvider.emitProviderReady(details);
        eventProvider.emitProviderConfigurationChanged(details);
        eventProvider.emitProviderStale(details);
        eventProvider.emitProviderError(details);

        Mockito.verify(onEmit, Mockito.timeout(TIMEOUT).times(2))
                .accept(eventProvider, ProviderEvent.PROVIDER_READY, details);
        Mockito.verify(onEmit, Mockito.timeout(TIMEOUT))
                .accept(eventProvider, ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
        Mockito.verify(onEmit, Mockito.timeout(TIMEOUT)).accept(eventProvider, ProviderEvent.PROVIDER_STALE, details);
        Mockito.verify(onEmit, Mockito.timeout(TIMEOUT)).accept(eventProvider, ProviderEvent.PROVIDER_ERROR, details);
    }

    @Test
    @DisplayName("should do nothing with emitters if no onEmit attached")
    void doesNotEmitsEventsWhenNotAttached() {
        // don't attach this emitter
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mockOnEmit();

        EventDetails details = EventDetails.EMPTY;
        eventProvider.emit(ProviderEvent.PROVIDER_READY, details);
        eventProvider.emitProviderReady(details);
        eventProvider.emitProviderConfigurationChanged(details);
        eventProvider.emitProviderStale(details);
        eventProvider.emitProviderError(details);

        // should not be called
        Mockito.verify(onEmit, Mockito.never())
                .accept(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("should throw if second different onEmit attached")
    void throwsWhenOnEmitDifferent() {
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit1 = mockOnEmit();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit2 = mockOnEmit();
        eventProvider.attach(onEmit1);
        assertThrows(IllegalStateException.class, () -> eventProvider.attach(onEmit2));
    }

    @Test
    @DisplayName("should not throw if second same onEmit attached")
    void doesNotThrowWhenOnEmitSame() {
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit1 = mockOnEmit();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit2 = onEmit1;
        eventProvider.attach(onEmit1);
        assertThatCode(() -> eventProvider.attach(onEmit2))
                .doesNotThrowAnyException(); // should not throw, same instance. noop
    }

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("should not deadlock on emit called during emit")
    void doesNotDeadlockOnEmitStackedCalls() throws Exception {
        TestStackedEmitCallsProvider provider = new TestStackedEmitCallsProvider();
        assertThatCode(() -> new DefaultOpenFeatureAPI().setProviderAndWait(provider))
                .doesNotThrowAnyException();
    }

    static class TestEventProvider extends AbstractEventProvider {

        private static final String NAME = "TestEventProvider";

        @Override
        public ProviderMetadata getMetadata() {
            return () -> NAME;
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getBooleanEvaluation'");
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getStringEvaluation'");
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getIntegerEvaluation'");
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getDoubleEvaluation'");
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getObjectEvaluation'");
        }

        @Override
        public void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
            super.attach(onEmit);
        }

        @Override
        public Provider addHooks(Hook<?>... hooks) {
            return this;
        }

        @Override
        public List<Hook<?>> getHooks() {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> mockOnEmit() {
        return (TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails>) Mockito.mock(TriConsumer.class);
    }
}

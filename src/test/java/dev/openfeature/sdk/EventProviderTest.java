package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.openfeature.sdk.internal.TriConsumer;

class EventProviderTest {

    @Test
    @DisplayName("should run attached onEmit with emitters")
    void emitsEventsWhenAttached() {
        TestEventProvider eventProvider = new TestEventProvider();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mockOnEmit();
        eventProvider.attach(onEmit);

        ProviderEventDetails details = ProviderEventDetails.builder().build();
        eventProvider.emit(ProviderEvent.PROVIDER_READY, details);
        eventProvider.emitProviderReady(details);
        eventProvider.emitProviderConfigurationChanged(details);
        eventProvider.emitProviderStale(details);
        eventProvider.emitProviderError(details);

        verify(onEmit, times(2)).accept(eventProvider, ProviderEvent.PROVIDER_READY, details);
        verify(onEmit, times(1)).accept(eventProvider, ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
        verify(onEmit, times(1)).accept(eventProvider, ProviderEvent.PROVIDER_STALE, details);
        verify(onEmit, times(1)).accept(eventProvider, ProviderEvent.PROVIDER_ERROR, details);
    }

    @Test
    @DisplayName("should do nothing with emitters if no onEmit attached")
    void doesNotEmitsEventsWhenNotAttached() {
        TestEventProvider eventProvider = new TestEventProvider();

        // don't attach this emitter
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mockOnEmit();

        ProviderEventDetails details = ProviderEventDetails.builder().build();
        eventProvider.emit(ProviderEvent.PROVIDER_READY, details);
        eventProvider.emitProviderReady(details);
        eventProvider.emitProviderConfigurationChanged(details);
        eventProvider.emitProviderStale(details);
        eventProvider.emitProviderError(details);

        // should not be called
        verify(onEmit, never()).accept(any(), any(), any());
    }

    @Test
    @DisplayName("should throw if second different onEmit attached")
    void throwsWhenOnEmitDifferent() {
        TestEventProvider eventProvider = new TestEventProvider();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit1 = mockOnEmit();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit2 = mockOnEmit();
        eventProvider.attach(onEmit1);
        assertThrows(IllegalStateException.class, () -> eventProvider.attach(onEmit2));
    }


    @Test
    @DisplayName("should not throw if second same onEmit attached")
    void doesNotThrowWhenOnEmitSame() {
        TestEventProvider eventProvider = new TestEventProvider();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit1 = mockOnEmit();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit2 = onEmit1;
        eventProvider.attach(onEmit1);
        eventProvider.attach(onEmit2); // should not throw, same instance. noop
    }


    class TestEventProvider extends EventProvider {

        private static final String NAME = "TestEventProvider";

        @Override
        public Metadata getMetadata() {
            return () -> NAME;
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue,
                EvaluationContext ctx) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getBooleanEvaluation'");
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue,
                EvaluationContext ctx) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getStringEvaluation'");
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue,
                EvaluationContext ctx) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getIntegerEvaluation'");
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue,
                EvaluationContext ctx) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getDoubleEvaluation'");
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
                EvaluationContext ctx) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getObjectEvaluation'");
        }

        @Override
        public ProviderState getState() {
            return ProviderState.READY;
        }
    }

    @SuppressWarnings("unchecked")
    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> mockOnEmit() {
        return (TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails>) mock(TriConsumer.class);
    }
}

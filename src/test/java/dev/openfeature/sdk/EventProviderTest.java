package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.FatalError;
import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.internal.TriConsumer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventProviderTest {

    private TestEventProvider eventProvider;

    @BeforeEach
    @SneakyThrows
    void setup() {
        eventProvider = new TestEventProvider();
        eventProvider.initialize(null);
    }

    @Test
    @DisplayName("should run attached onEmit with emitters")
    void emitsEventsWhenAttached() {
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
        eventProvider.attach(onEmit2); // should not throw, same instance. noop
    }

    @Test
    @DisplayName("the state is NOT_READY before initialize method is called")
    void stateNotReadyBeforeCallingInit() {
        AtomicBoolean doInitializationCalled = new AtomicBoolean();
        EventProvider provider = new TestEventProvider() {
            @Override
            protected void doInitialization(EvaluationContext evaluationContext) {
                doInitializationCalled.set(true);
            }
        };

        assertThat(provider.getState()).isEqualTo(ProviderState.NOT_READY);
        assertThat(doInitializationCalled).isFalse();
    }

    @Test
    @SneakyThrows
    @DisplayName("sets the state to READY after running the initialize method")
    void setsStateToReadyAfterInit() {
        AtomicBoolean doInitializationCalled = new AtomicBoolean();
        EventProvider provider = new TestEventProvider() {
            @Override
            protected void doInitialization(EvaluationContext evaluationContext) {
                doInitializationCalled.set(true);
            }
        };
        provider.initialize(null);
        assertThat(provider.getState()).isEqualTo(ProviderState.READY);
        assertThat(doInitializationCalled).isTrue();
    }

    @Test
    @DisplayName("sets the state to ERROR when the doInitialization method throws an error")
    void setsStateToErrorWhenFailingInit() {
        AtomicBoolean doInitializationCalled = new AtomicBoolean();
        EventProvider provider = new TestEventProvider() {
            @Override
            protected void doInitialization(EvaluationContext evaluationContext) {
                doInitializationCalled.set(true);
                throw new RuntimeException();
            }
        };
        assertThrows(GeneralError.class, () -> provider.initialize(null));
        assertThat(provider.getState()).isEqualTo(ProviderState.ERROR);
        assertThat(doInitializationCalled).isTrue();
    }

    @Test
    @DisplayName("sets the state to PROVIDER_FATAL when the doInitialization method throws a fatal error")
    void setsStateToFatalWhenFailingInit() {
        AtomicBoolean doInitializationCalled = new AtomicBoolean();
        EventProvider provider = new TestEventProvider() {
            @Override
            protected void doInitialization(EvaluationContext evaluationContext) {
                doInitializationCalled.set(true);
                throw new FatalError();
            }
        };
        assertThrows(FatalError.class, () -> provider.initialize(null));
        assertThat(provider.getState()).isEqualTo(ProviderState.FATAL);
        assertThat(doInitializationCalled).isTrue();
    }

    @Test
    @Specification(number = "5.3.5", text = "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @DisplayName("sets the state to ERROR when an error event is emitted")
    void setsStateToErrorWhenErrorEventIsEmitted() {
        EventProvider provider = new TestEventProvider() {
            @Override
            protected void doInitialization(EvaluationContext evaluationContext) {
            }
        };
        assertThat(provider.getState()).isNotEqualTo(ProviderState.ERROR);
        provider.emitProviderError(ProviderEventDetails.builder().build());
        assertThat(provider.getState()).isEqualTo(ProviderState.ERROR);
    }

    @Test
    @Specification(number = "5.3.5", text = "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @DisplayName("sets the state to STALE when a stale event is emitted")
    void setsStateToStaleWhenStaleEventIsEmitted() {
        EventProvider provider = new TestEventProvider() {
            @Override
            protected void doInitialization(EvaluationContext evaluationContext) {
            }
        };
        assertThat(provider.getState()).isNotEqualTo(ProviderState.STALE);
        provider.emitProviderStale(ProviderEventDetails.builder().build());
        assertThat(provider.getState()).isEqualTo(ProviderState.STALE);
    }

    @Test
    @Specification(number = "5.3.5", text = "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @DisplayName("sets the state to READY when a ready event is emitted")
    void setsStateToReadyWhenReadyEventIsEmitted() {
        EventProvider provider = new TestEventProvider() {
            @Override
            protected void doInitialization(EvaluationContext evaluationContext) {
            }
        };
        provider.emitProviderStale(ProviderEventDetails.builder().build());
        assertThat(provider.getState()).isNotEqualTo(ProviderState.READY);
        provider.emitProviderReady(ProviderEventDetails.builder().build());
        assertThat(provider.getState()).isEqualTo(ProviderState.READY);
    }

    static class TestEventProvider extends EventProvider {

        private static final String NAME = "TestEventProvider";

        @Override
        public Metadata getMetadata() {
            return () -> NAME;
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue,
                                                                EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getBooleanEvaluation'");
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue,
                                                              EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getStringEvaluation'");
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue,
                                                                EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getIntegerEvaluation'");
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue,
                                                              EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getDoubleEvaluation'");
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
                                                             EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getObjectEvaluation'");
        }

        @Override
        public void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
            super.attach(onEmit);
        }
    }

    @SuppressWarnings("unchecked")
    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> mockOnEmit() {
        return (TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails>) mock(TriConsumer.class);
    }
}
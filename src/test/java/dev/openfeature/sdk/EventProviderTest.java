package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import dev.openfeature.sdk.internal.TriConsumer;
import dev.openfeature.sdk.testutils.TestStackedEmitCallsProvider;
import io.cucumber.java.AfterAll;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class EventProviderTest {

    private static final int TIMEOUT = 300;

    private TestEventProvider eventProvider;

    @BeforeEach
    @SneakyThrows
    void setup() {
        eventProvider = new TestEventProvider();
        eventProvider.initialize(null);
    }

    @AfterAll
    public static void resetDefaultProvider() {
        new OpenFeatureAPI().setProviderAndWait(new NoOpProvider());
    }

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("should run attached onEmit with emitters")
    void emitsEventsWhenAttached() {
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mockOnEmit();
        eventProvider.attach(onEmit, new AutoCloseableReentrantReadWriteLock());

        ProviderEventDetails details = ProviderEventDetails.builder().build();
        eventProvider.emit(ProviderEvent.PROVIDER_READY, details);
        eventProvider.emitProviderReady(details);
        eventProvider.emitProviderConfigurationChanged(details);
        eventProvider.emitProviderStale(details);
        eventProvider.emitProviderError(details);

        verify(onEmit, timeout(TIMEOUT).times(2)).accept(eventProvider, ProviderEvent.PROVIDER_READY, details);
        verify(onEmit, timeout(TIMEOUT)).accept(eventProvider, ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
        verify(onEmit, timeout(TIMEOUT)).accept(eventProvider, ProviderEvent.PROVIDER_STALE, details);
        verify(onEmit, timeout(TIMEOUT)).accept(eventProvider, ProviderEvent.PROVIDER_ERROR, details);
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
        AutoCloseableReentrantReadWriteLock lock = new AutoCloseableReentrantReadWriteLock();
        eventProvider.attach(onEmit1, lock);
        assertThrows(IllegalStateException.class, () -> eventProvider.attach(onEmit2, lock));
    }

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("emit should acquire read lock when attached")
    void emitAcquiresReadLockWhenAttached() throws Exception {
        AutoCloseableReentrantReadWriteLock lock = new AutoCloseableReentrantReadWriteLock();
        CountDownLatch lockAcquired = new CountDownLatch(1);

        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = (ep, event, details) -> {
            // When the onEmit callback runs, the read lock must already be held
            assertThat(lock.getReadLockCount()).isGreaterThan(0);
            lockAcquired.countDown();
        };

        eventProvider.attach(onEmit, lock);
        eventProvider.emit(
                ProviderEvent.PROVIDER_READY, ProviderEventDetails.builder().build());

        assertThat(lockAcquired.await(1, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("emit should not acquire lock when not attached")
    void emitDoesNotAcquireLockWhenNotAttached() {
        // emit without attaching — should return immediately without error
        Awaitable result = eventProvider.emit(
                ProviderEvent.PROVIDER_READY, ProviderEventDetails.builder().build());
        assertThat(result).isSameAs(Awaitable.FINISHED);
    }

    @Test
    @SneakyThrows
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("should not deadlock on emit called during emit")
    void doesNotDeadlockOnEmitStackedCalls() {
        TestStackedEmitCallsProvider provider = new TestStackedEmitCallsProvider();
        new OpenFeatureAPI().setProviderAndWait(provider);
    }

    static class TestEventProvider extends EventProvider {

        private static final String NAME = "TestEventProvider";

        @Override
        public Metadata getMetadata() {
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
        public void attach(
                TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit,
                AutoCloseableReentrantReadWriteLock lock) {
            super.attach(onEmit, lock);
        }
    }

    @SuppressWarnings("unchecked")
    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> mockOnEmit() {
        return (TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails>) mock(TriConsumer.class);
    }
}

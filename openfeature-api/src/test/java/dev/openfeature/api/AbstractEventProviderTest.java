package dev.openfeature.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.events.EventEmitter;
import dev.openfeature.api.events.EventProvider;
import dev.openfeature.api.events.ProviderEventDetails;
import dev.openfeature.api.internal.TriConsumer;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractEventProviderTest {

    private TestEventProvider provider;
    private TestEventEmitter testEventEmitter;
    private ProviderEventDetails testEventDetails;

    @BeforeEach
    void setUp() {
        provider = new TestEventProvider();
        testEventEmitter = new TestEventEmitter();
        testEventDetails = ProviderEventDetails.of("Test event", List.of("test-flag"));
    }

    @Specification(
            number = "2.3.1",
            text =
                    "The provider interface MUST define a provider hook mechanism which can be optionally implemented in order to add hook instances to the evaluation life-cycle.")
    @Test
    void supports_hook_management() {
        TestHook hook1 = new TestHook("hook1");
        TestHook hook2 = new TestHook("hook2");

        // Initially no hooks
        assertThat(provider.getHooks()).isNotNull().isEmpty();

        // Add hooks and verify fluent API
        Provider result = provider.addHooks(hook1, hook2);

        assertThat(result).isSameAs(provider);

        assertThat(provider.getHooks()).hasSize(2).containsExactly(hook1, hook2);
    }

    @Specification(
            number = "2.3.1",
            text =
                    "The provider interface MUST define a provider hook mechanism which can be optionally implemented in order to add hook instances to the evaluation life-cycle.")
    @Test
    void hook_management_handles_null_hooks_list() {
        TestHook hook = new TestHook("test-hook");

        // Add hook when hooks list is null (initial state)
        provider.addHooks(hook);

        assertThat(provider.getHooks()).hasSize(1).containsExactly(hook);
    }

    @Test
    void get_hooks_returns_immutable_copy() {
        TestHook hook = new TestHook("test-hook");
        provider.addHooks(hook);

        List<Hook<?>> hooks = provider.getHooks();

        // Should be immutable - cannot modify returned list
        assertThatThrownBy(() -> hooks.add(new TestHook("another-hook")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void clear_hooks_removes_all_hooks() {
        TestHook hook1 = new TestHook("hook1");
        TestHook hook2 = new TestHook("hook2");

        provider.addHooks(hook1, hook2);
        assertThat(provider.getHooks()).hasSize(2);

        provider.clearHooks();
        assertThat(provider.getHooks()).isEmpty();
    }

    @Test
    void clear_hooks_handles_null_hooks_list() {
        // Should not throw when hooks list is null (initial state)
        assertThatCode(() -> provider.clearHooks()).doesNotThrowAnyException();

        assertThat(provider.getHooks()).isEmpty();
    }

    @Test
    void set_event_emitter_stores_emitter() {
        provider.setEventEmitter(testEventEmitter);

        // Verify emitter is stored by testing dependent operations
        provider.attach(testEventEmitter.getTestAttachConsumer());

        assertThat(testEventEmitter.isAttached()).isTrue();
    }

    @Test
    void attach_delegates_to_event_emitter() {
        provider.setEventEmitter(testEventEmitter);
        TestTriConsumer consumer = new TestTriConsumer();

        provider.attach(consumer);

        assertThat(testEventEmitter.isAttached()).isTrue();
        assertThat(testEventEmitter.getAttachedConsumer()).isSameAs(consumer);
    }

    @Test
    void attach_handles_null_event_emitter() {
        TestTriConsumer consumer = new TestTriConsumer();

        // Should not throw when event emitter is null
        assertThatCode(() -> provider.attach(consumer)).doesNotThrowAnyException();
    }

    @Test
    void detach_delegates_to_event_emitter() {
        provider.setEventEmitter(testEventEmitter);
        TestTriConsumer consumer = new TestTriConsumer();

        // First attach, then detach
        provider.attach(consumer);
        assertThat(testEventEmitter.isAttached()).isTrue();

        provider.detach();
        assertThat(testEventEmitter.isAttached()).isFalse();
    }

    @Test
    void detach_handles_null_event_emitter() {
        // Should not throw when event emitter is null
        assertThatCode(() -> provider.detach()).doesNotThrowAnyException();
    }

    @Test
    void emit_delegates_to_event_emitter() {
        provider.setEventEmitter(testEventEmitter);

        Awaitable result = provider.emit(ProviderEvent.PROVIDER_READY, testEventDetails);

        assertThat(result).isNotNull().isSameAs(Awaitable.FINISHED); // TestEventEmitter returns FINISHED

        assertThat(testEventEmitter.getLastEmittedEvent()).isEqualTo(ProviderEvent.PROVIDER_READY);
        assertThat(testEventEmitter.getLastEmittedDetails()).isSameAs(testEventDetails);
    }

    @Test
    void emit_returns_awaitable_that_completes_immediately() {
        provider.setEventEmitter(testEventEmitter);

        Awaitable result = provider.emit(ProviderEvent.PROVIDER_READY, testEventDetails);

        assertThat(result).isNotNull().isSameAs(Awaitable.FINISHED);

        // Should complete immediately without blocking
        assertThatCode(() -> result.await()).doesNotThrowAnyException();
    }

    @Test
    void emit_returns_null_when_event_emitter_is_null() {
        // When no event emitter is set
        Awaitable result = provider.emit(ProviderEvent.PROVIDER_READY, testEventDetails);

        assertThat(result).isNull();
    }

    @Specification(
            number = "2.5.1",
            text = "The provider MAY define a mechanism to gracefully shutdown and dispose of resources.")
    @Test
    void shutdown_delegates_to_event_emitter() {
        provider.setEventEmitter(testEventEmitter);

        provider.shutdown();

        assertThat(testEventEmitter.isShutdown()).isTrue();
    }

    @Test
    void shutdown_handles_null_event_emitter() {
        // Should not throw when event emitter is null
        assertThatCode(() -> provider.shutdown()).doesNotThrowAnyException();
    }

    @Test
    void supports_all_provider_event_types() {
        provider.setEventEmitter(testEventEmitter);

        // Test all standard provider events
        provider.emit(ProviderEvent.PROVIDER_READY, testEventDetails);
        assertThat(testEventEmitter.getLastEmittedEvent()).isEqualTo(ProviderEvent.PROVIDER_READY);

        provider.emit(ProviderEvent.PROVIDER_ERROR, testEventDetails);
        assertThat(testEventEmitter.getLastEmittedEvent()).isEqualTo(ProviderEvent.PROVIDER_ERROR);

        provider.emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, testEventDetails);
        assertThat(testEventEmitter.getLastEmittedEvent()).isEqualTo(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED);

        provider.emit(ProviderEvent.PROVIDER_STALE, testEventDetails);
        assertThat(testEventEmitter.getLastEmittedEvent()).isEqualTo(ProviderEvent.PROVIDER_STALE);

        assertThat(testEventEmitter.getEmitCount()).isEqualTo(4);
    }

    @Test
    void multiple_hooks_added_in_order() {
        TestHook hook1 = new TestHook("hook1");
        TestHook hook2 = new TestHook("hook2");
        TestHook hook3 = new TestHook("hook3");

        provider.addHooks(hook1, hook2);
        provider.addHooks(hook3);

        assertThat(provider.getHooks()).hasSize(3).containsExactly(hook1, hook2, hook3);
    }

    @Test
    void event_emitter_can_be_replaced() {
        TestEventEmitter emitter1 = new TestEventEmitter();
        TestEventEmitter emitter2 = new TestEventEmitter();

        // Set first emitter
        provider.setEventEmitter(emitter1);
        provider.emit(ProviderEvent.PROVIDER_READY, testEventDetails);
        assertThat(emitter1.getEmitCount()).isEqualTo(1);

        // Replace with second emitter
        provider.setEventEmitter(emitter2);
        provider.emit(ProviderEvent.PROVIDER_ERROR, testEventDetails);
        assertThat(emitter2.getEmitCount()).isEqualTo(1);
        assertThat(emitter1.getEmitCount()).isEqualTo(1); // Should remain unchanged
    }

    @Specification(
            number = "2.3.1",
            text =
                    "The provider interface MUST define a provider hook mechanism which can be optionally implemented in order to add hook instances to the evaluation life-cycle.")
    @Test
    void supports_fluent_hook_api() {
        TestHook hook1 = new TestHook("hook1");
        TestHook hook2 = new TestHook("hook2");

        // Should support method chaining
        Provider result = provider.addHooks(hook1).addHooks(hook2);

        assertThat(result).isSameAs(provider);

        assertThat(provider.getHooks()).containsExactly(hook1, hook2);
    }

    @Test
    void event_details_are_passed_correctly() {
        provider.setEventEmitter(testEventEmitter);

        ProviderEventDetails customDetails = ProviderEventDetails.of("Custom test message", List.of("flag1", "flag2"));

        provider.emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, customDetails);

        assertThat(testEventEmitter.getLastEmittedEvent()).isEqualTo(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED);
        assertThat(testEventEmitter.getLastEmittedDetails()).isSameAs(customDetails);
        assertThat(testEventEmitter.getLastEmittedDetails().getFlagsChanged()).containsExactly("flag1", "flag2");
    }

    @Test
    void hooks_can_be_added_multiple_times() {
        TestHook hook1 = new TestHook("hook1");
        TestHook hook2 = new TestHook("hook2");
        TestHook hook3 = new TestHook("hook3");

        // Add hooks in multiple calls
        provider.addHooks(hook1);
        provider.addHooks(hook2, hook3);

        assertThat(provider.getHooks()).hasSize(3).containsExactly(hook1, hook2, hook3);
    }

    @Test
    void awaitable_synchronization_behavior() {
        // Test with a custom awaitable that demonstrates proper synchronization
        TestEventEmitterWithCustomAwaitable customEmitter = new TestEventEmitterWithCustomAwaitable();
        provider.setEventEmitter(customEmitter);

        Awaitable result = provider.emit(ProviderEvent.PROVIDER_READY, testEventDetails);

        assertThat(result).isNotNull();

        // Initially not done
        assertThat(customEmitter.getLastAwaitable().isDone()).isFalse();

        // Complete the awaitable
        customEmitter.getLastAwaitable().wakeup();

        // Now should complete immediately
        assertThatCode(() -> result.await()).doesNotThrowAnyException();

        assertThat(customEmitter.getLastAwaitable().isDone()).isTrue();
    }

    @Test
    void empty_hooks_array_handled_gracefully() {
        provider.addHooks(); // Empty varargs

        assertThat(provider.getHooks()).isEmpty();
    }

    // Test helper classes - Simple implementations without mocking

    private static class TestEventProvider extends AbstractEventProvider {

        @Override
        public ProviderMetadata getMetadata() {
            return () -> "Test Event Provider";
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, null, null);
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(
                String key, String defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, null, null);
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, null, null);
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(
                String key, Double defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, null, null);
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(
                String key, Value defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, null, null);
        }
    }

    private static class TestEventEmitter implements EventEmitter {
        private boolean attached = false;
        private boolean shutdown = false;
        private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> attachedConsumer;
        private ProviderEvent lastEmittedEvent;
        private ProviderEventDetails lastEmittedDetails;
        private final AtomicInteger emitCount = new AtomicInteger(0);

        @Override
        public void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
            this.attached = true;
            this.attachedConsumer = onEmit;
        }

        @Override
        public void detach() {
            this.attached = false;
            this.attachedConsumer = null;
        }

        @Override
        public Awaitable emit(ProviderEvent event, ProviderEventDetails details) {
            this.lastEmittedEvent = event;
            this.lastEmittedDetails = details;
            emitCount.incrementAndGet();
            return Awaitable.FINISHED; // Return the real finished awaitable
        }

        @Override
        public void shutdown() {
            this.shutdown = true;
        }

        // Test helper methods
        public boolean isAttached() {
            return attached;
        }

        public boolean isShutdown() {
            return shutdown;
        }

        public TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> getAttachedConsumer() {
            return attachedConsumer;
        }

        public ProviderEvent getLastEmittedEvent() {
            return lastEmittedEvent;
        }

        public ProviderEventDetails getLastEmittedDetails() {
            return lastEmittedDetails;
        }

        public int getEmitCount() {
            return emitCount.get();
        }

        public TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> getTestAttachConsumer() {
            return new TestTriConsumer();
        }
    }

    private static class TestTriConsumer implements TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> {
        private EventProvider lastProvider;
        private ProviderEvent lastEvent;
        private ProviderEventDetails lastDetails;

        @Override
        public void accept(EventProvider provider, ProviderEvent event, ProviderEventDetails details) {
            this.lastProvider = provider;
            this.lastEvent = event;
            this.lastDetails = details;
        }

        // Test helper methods
        public EventProvider getLastProvider() {
            return lastProvider;
        }

        public ProviderEvent getLastEvent() {
            return lastEvent;
        }

        public ProviderEventDetails getLastDetails() {
            return lastDetails;
        }
    }

    private static class TestHook implements Hook<Void> {
        private final String name;

        public TestHook(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            TestHook testHook = (TestHook) obj;
            return name.equals(testHook.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "TestHook{name='" + name + "'}";
        }
    }

    // Additional test emitter that uses custom awaitable for testing synchronization
    private static class TestEventEmitterWithCustomAwaitable implements EventEmitter {
        private TestableAwaitable lastAwaitable;

        @Override
        public void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
            // No-op for this test
        }

        @Override
        public void detach() {
            // No-op for this test
        }

        @Override
        public Awaitable emit(ProviderEvent event, ProviderEventDetails details) {
            lastAwaitable = new TestableAwaitable();
            return lastAwaitable;
        }

        @Override
        public void shutdown() {
            // No-op for this test
        }

        public TestableAwaitable getLastAwaitable() {
            return lastAwaitable;
        }
    }

    // Testable version of Awaitable that exposes internal state
    private static class TestableAwaitable extends Awaitable {
        private boolean done = false;

        @Override
        public synchronized void wakeup() {
            done = true;
            super.wakeup();
        }

        public boolean isDone() {
            return done;
        }
    }
}

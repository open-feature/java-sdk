package dev.openfeature.api;

import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.events.EventEmitter;
import dev.openfeature.api.events.EventProvider;
import dev.openfeature.api.events.ProviderEventDetails;
import dev.openfeature.api.internal.TriConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract EventProvider. Providers must extend this class to support events.
 * Emit events with {@link #emit(ProviderEvent, ProviderEventDetails)}. Please
 * note that the SDK will automatically emit
 * {@link ProviderEvent#PROVIDER_READY } or
 * {@link ProviderEvent#PROVIDER_ERROR } accordingly when
 * {@link Provider#initialize(EvaluationContext)} completes successfully
 * or with error, so these events need not be emitted manually during
 * initialization.
 *
 * @see Provider
 */
public abstract class AbstractEventProvider implements EventProvider {
    private static final Logger log = LoggerFactory.getLogger(AbstractEventProvider.class);
    private EventEmitter eventEmitter;
    private final ExecutorService emitterExecutor = Executors.newCachedThreadPool();
    private List<Hook<?>> hooks;

    public void setEventEmitter(EventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }

    /**
     * "Attach" this EventProvider to an SDK, which allows events to propagate from this provider.
     * No-op if the same onEmit is already attached.
     *
     * @param onEmit the function to run when a provider emits events.
     * @throws IllegalStateException if attempted to bind a new emitter for already bound provider
     */
    public void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
        if (eventEmitter == null) {
            return;
        }
        eventEmitter.attach(onEmit);
    }

    /**
     * "Detach" this EventProvider from an SDK, stopping propagation of all events.
     */
    public void detach() {
        if (eventEmitter == null) {
            return;
        }
        eventEmitter.detach();
    }

    /**
     * Stop the event emitter executor and block until either termination has completed
     * or timeout period has elapsed.
     */
    @Override
    public void shutdown() {
        if (eventEmitter == null) {
            return;
        }
        eventEmitter.shutdown();
    }

    /**
     * Emit the specified {@link ProviderEvent}.
     *
     * @param event   The event type
     * @param details The details of the event
     */
    public Awaitable emit(final ProviderEvent event, final ProviderEventDetails details) {
        return eventEmitter.emit(event, details);
    }

    @Override
    public Provider addHooks(Hook<?>... hooks) {
        if (this.hooks == null) {
            this.hooks = new ArrayList<>();
        }
        this.hooks.addAll(List.of(hooks));
        return this;
    }

    @Override
    public List<Hook<?>> getHooks() {
        if (hooks == null) {
            return List.of();
        }
        return List.copyOf(hooks);
    }

    @Override
    public void clearHooks() {
        if (hooks == null) {
            return;
        }
        hooks.clear();
    }
}

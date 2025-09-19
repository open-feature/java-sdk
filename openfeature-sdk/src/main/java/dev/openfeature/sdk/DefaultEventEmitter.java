package dev.openfeature.sdk;

import dev.openfeature.api.Awaitable;
import dev.openfeature.api.Provider;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.events.EventEmitter;
import dev.openfeature.api.events.EventProvider;
import dev.openfeature.api.events.ProviderEventDetails;
import dev.openfeature.api.internal.TriConsumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
class DefaultEventEmitter implements EventEmitter {
    private static final Logger log = LoggerFactory.getLogger(DefaultEventEmitter.class);
    private final EventProviderListener eventProviderListener;
    private final ExecutorService emitterExecutor = Executors.newCachedThreadPool();
    private final EventProvider provider;

    protected DefaultEventEmitter(EventProvider provider, EventProviderListener eventProviderListener) {
        this.provider = provider;
        this.eventProviderListener = eventProviderListener;
    }

    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = null;

    /**
     * "Attach" this EventProvider to an SDK, which allows events to propagate from this provider.
     * No-op if the same onEmit is already attached.
     *
     * @param onEmit the function to run when a provider emits events.
     * @throws IllegalStateException if attempted to bind a new emitter for already bound provider
     */
    @Override
    public void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
        if (this.onEmit != null && this.onEmit != onEmit) {
            // if we are trying to attach this provider to a different onEmit, something has gone wrong
            throw new IllegalStateException("Provider " + provider.getMetadata().getName() + " is already attached.");
        } else {
            this.onEmit = onEmit;
        }
    }

    /**
     * "Detach" this EventProvider from an SDK, stopping propagation of all events.
     */
    public void detach() {
        this.onEmit = null;
    }

    /**
     * Stop the event emitter executor and block until either termination has completed
     * or timeout period has elapsed.
     */
    @Override
    public void shutdown() {
        emitterExecutor.shutdown();
        try {
            if (!emitterExecutor.awaitTermination(EventSupport.SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Emitter executor did not terminate before the timeout period had elapsed");
                emitterExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            emitterExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Emit the specified {@link ProviderEvent}.
     *
     * @param event   The event type
     * @param details The details of the event
     */
    public Awaitable emit(final ProviderEvent event, final ProviderEventDetails details) {
        final var localEventProviderListener = this.eventProviderListener;
        final var localOnEmit = this.onEmit;

        if (localEventProviderListener == null && localOnEmit == null) {
            return Awaitable.FINISHED;
        }

        final var awaitable = new Awaitable();

        // These calls need to be executed on a different thread to prevent deadlocks when the provider initialization
        // relies on a ready event to be emitted
        emitterExecutor.submit(() -> {
            try (var ignored = DefaultOpenFeatureAPI.lock.readLockAutoCloseable()) {
                if (localEventProviderListener != null) {
                    localEventProviderListener.onEmit(event, details);
                }
                if (localOnEmit != null) {
                    localOnEmit.accept(provider, event, details);
                }
            } finally {
                awaitable.wakeup();
            }
        });

        return awaitable;
    }
}

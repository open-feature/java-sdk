package dev.openfeature.sdk;

import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import dev.openfeature.sdk.internal.ConfigurableThreadFactory;
import dev.openfeature.sdk.internal.TriConsumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract EventProvider. Providers must extend this class to support events.
 * Emit events with {@link #emit(ProviderEvent, ProviderEventDetails)}. Please
 * note that the SDK will automatically emit
 * {@link ProviderEvent#PROVIDER_READY } or
 * {@link ProviderEvent#PROVIDER_ERROR } accordingly when
 * {@link FeatureProvider#initialize(EvaluationContext)} completes successfully
 * or with error, so these events need not be emitted manually during
 * initialization.
 *
 * @see FeatureProvider
 */
@Slf4j
public abstract class EventProvider implements FeatureProvider {
    private EventProviderListener eventProviderListener;
    private final ExecutorService emitterExecutor =
            Executors.newCachedThreadPool(new ConfigurableThreadFactory("openfeature-event-emitter-thread", true));

    void setEventProviderListener(EventProviderListener eventProviderListener) {
        this.eventProviderListener = eventProviderListener;
    }

    // Bundles onEmit and lock into a single volatile reference so they are always read atomically:
    // a non-null attachment guarantees a non-null lock.
    private static final class Attachment {
        final TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit;
        final AutoCloseableReentrantReadWriteLock lock;

        Attachment(
                TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit,
                AutoCloseableReentrantReadWriteLock lock) {
            this.onEmit = onEmit;
            this.lock = lock;
        }
    }

    private volatile Attachment attachment = null;

    /**
     * "Attach" this EventProvider to an SDK, which allows events to propagate from this provider.
     * No-op if the same onEmit is already attached.
     *
     * @param onEmit the function to run when a provider emits events.
     * @param lock   the API instance's read/write lock for thread safety.
     * @throws IllegalStateException if attempted to bind a new emitter for already bound provider
     */
    void attach(
            TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit,
            AutoCloseableReentrantReadWriteLock lock) {
        Attachment existing = this.attachment;
        if (existing != null && existing.onEmit != onEmit) {
            // if we are trying to attach this provider to a different onEmit, something has gone wrong
            throw new IllegalStateException("Provider " + this.getMetadata().getName() + " is already attached.");
        }
        this.attachment = new Attachment(onEmit, lock);
    }

    /**
     * "Detach" this EventProvider from an SDK, stopping propagation of all events.
     */
    void detach() {
        this.attachment = null;
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
        final var localAttachment = this.attachment;

        if (localEventProviderListener == null && localAttachment == null) {
            return Awaitable.FINISHED;
        }

        final var awaitable = new Awaitable();

        // These calls need to be executed on a different thread to prevent deadlocks when the provider initialization
        // relies on a ready event to be emitted
        emitterExecutor.submit(() -> {
            // Lock is only needed when attached to an API instance. A non-null attachment always
            // carries a non-null lock, so no null check on the lock itself is required.
            try (var ignored = localAttachment != null ? localAttachment.lock.readLockAutoCloseable() : null) {
                if (localEventProviderListener != null) {
                    localEventProviderListener.onEmit(event, details);
                }
                if (localAttachment != null) {
                    localAttachment.onEmit.accept(this, event, details);
                }
            } finally {
                awaitable.wakeup();
            }
        });

        return awaitable;
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_READY} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public Awaitable emitProviderReady(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_READY, details);
    }

    /**
     * Emit a
     * {@link ProviderEvent#PROVIDER_CONFIGURATION_CHANGED}
     * event. Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public Awaitable emitProviderConfigurationChanged(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_STALE} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public Awaitable emitProviderStale(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_STALE, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_ERROR} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public Awaitable emitProviderError(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_ERROR, details);
    }
}

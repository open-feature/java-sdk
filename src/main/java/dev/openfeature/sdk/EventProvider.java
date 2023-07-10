package dev.openfeature.sdk;

import org.apache.logging.log4j.util.TriConsumer;

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
public abstract class EventProvider implements FeatureProvider {

    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = null;

    void detach() {
        this.onEmit = null;
    }

    void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
        if (this.onEmit == null) {
            this.onEmit = onEmit;
        }
    }

    /**
     * Emit the specified {@link dev.openfeature.sdk.ProviderEvent}.
     * 
     * @param event   The event type
     * @param details The details of the event
     */
    public void emit(ProviderEvent event, ProviderEventDetails details) {
        if (this.onEmit != null) {
            this.onEmit.accept(this, event, details);
        }
    }

    /**
     * Emit a {@link dev.openfeature.sdk.ProviderEvent#PROVIDER_READY} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     * 
     * @param details The details of the event
     */
    public void emitProviderReady(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_READY, details);
    }

    /**
     * Emit a
     * {@link dev.openfeature.sdk.ProviderEvent#PROVIDER_CONFIGURATION_CHANGED}
     * event. Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     * 
     * @param details The details of the event
     */
    public void emitProviderConfigurationChanged(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
    }

    /**
     * Emit a {@link dev.openfeature.sdk.ProviderEvent#PROVIDER_STALE} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     * 
     * @param details The details of the event
     */
    public void emitProviderStale(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_STALE, details);
    }

    /**
     * Emit a {@link dev.openfeature.sdk.ProviderEvent#PROVIDER_ERROR} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     * 
     * @param details The details of the event
     */
    public void emitProviderError(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_ERROR, details);
    }
}

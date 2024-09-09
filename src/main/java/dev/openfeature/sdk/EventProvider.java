package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.internal.TriConsumer;

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

    private ProviderState providerState = ProviderState.NOT_READY;

    /**
     * {@inheritDoc}
     */
    @Override
    public final ProviderState getState() {
        return providerState;
    }

    @Override
    public final void initialize(EvaluationContext evaluationContext) throws Exception {
        try {
            doInitialization(evaluationContext);
            providerState = ProviderState.READY;
        } catch (OpenFeatureError openFeatureError) {
            if (ErrorCode.PROVIDER_FATAL.equals(openFeatureError.getErrorCode())) {
                providerState = ProviderState.FATAL;
            } else {
                providerState = ProviderState.ERROR;
            }
            throw openFeatureError;
        } catch (Exception e) {
            providerState = ProviderState.ERROR;
            throw new GeneralError(e);
        }
    }

    protected void doInitialization(EvaluationContext evaluationContext) throws Exception {
        // Intentionally left blank, to be implemented by inheritors
    }

    @Override
    public final void shutdown() {
        providerState = ProviderState.NOT_READY;
        doShutdown();
    }

    protected void doShutdown() {
        // Intentionally left blank, to be implemented by inheritors
    }

    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = null;

    /**
     * "Attach" this EventProvider to an SDK, which allows events to propagate from this provider.
     * No-op if the same onEmit is already attached.
     *
     * @param onEmit the function to run when a provider emits events.
     * @throws IllegalStateException if attempted to bind a new emitter for already bound provider
     */
    void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
        if (this.onEmit != null && this.onEmit != onEmit) {
            // if we are trying to attach this provider to a different onEmit, something has gone wrong
            throw new IllegalStateException("Provider " + this.getMetadata().getName() + " is already attached.");
        } else {
            this.onEmit = onEmit;
        }
    }

    /**
     * "Detach" this EventProvider from an SDK, stopping propagation of all events.
     */
    void detach() {
        this.onEmit = null;
    }

    /**
     * Emit the specified {@link ProviderEvent}.
     *
     * @param event   The event type
     * @param details The details of the event
     */
    public void emit(ProviderEvent event, ProviderEventDetails details) {
        if (ProviderEvent.PROVIDER_ERROR.equals(event)) {
            providerState = ProviderState.ERROR;
        } else if (ProviderEvent.PROVIDER_STALE.equals(event)) {
            providerState = ProviderState.STALE;
        } else if (ProviderEvent.PROVIDER_READY.equals(event)) {
            providerState = ProviderState.READY;
        }
        if (this.onEmit != null) {
            this.onEmit.accept(this, event, details);
        }
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_READY} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderReady(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_READY, details);
    }

    /**
     * Emit a
     * {@link ProviderEvent#PROVIDER_CONFIGURATION_CHANGED}
     * event. Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderConfigurationChanged(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_STALE} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderStale(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_STALE, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_ERROR} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderError(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_ERROR, details);
    }
}

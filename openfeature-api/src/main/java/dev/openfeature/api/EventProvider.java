package dev.openfeature.api;

/**
 * Interface for feature providers that support real-time events.
 * Providers can implement this interface to emit events about flag changes,
 * provider state changes, and other configuration updates.
 *
 * @see FeatureProvider
 */
public interface EventProvider extends FeatureProvider {

    /**
     * Emit the specified {@link ProviderEvent}.
     *
     * @param event   The event type
     * @param details The details of the event
     * @return An {@link Awaitable} that can be used to wait for event processing completion
     */
    Awaitable emit(ProviderEvent event, ProviderEventDetails details);

    /**
     * Emit a {@link ProviderEvent#PROVIDER_READY} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     * @return An {@link Awaitable} that can be used to wait for event processing completion
     */
    default Awaitable emitProviderReady(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_READY, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_CONFIGURATION_CHANGED} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     * @return An {@link Awaitable} that can be used to wait for event processing completion
     */
    default Awaitable emitProviderConfigurationChanged(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_STALE} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     * @return An {@link Awaitable} that can be used to wait for event processing completion
     */
    default Awaitable emitProviderStale(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_STALE, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_ERROR} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     * @return An {@link Awaitable} that can be used to wait for event processing completion
     */
    default Awaitable emitProviderError(ProviderEventDetails details) {
        return emit(ProviderEvent.PROVIDER_ERROR, details);
    }
}

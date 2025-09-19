package dev.openfeature.api.events;

import dev.openfeature.api.Awaitable;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.internal.TriConsumer;
import dev.openfeature.api.lifecycle.Lifecycle;

/**
 * EventEmitter can be passed in to provide event emitting functionality from outside.
 */
public interface EventEmitter extends Lifecycle {
    void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit);

    void detach();

    Awaitable emit(final ProviderEvent event, final ProviderEventDetails details);
}

package dev.openfeature.sdk;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.openfeature.sdk.internal.TriConsumer;
import dev.openfeature.sdk.testutils.TestEventsProvider;

class EventProviderTest {

    @Nested
    @DisplayName("if attached")
    class IfAttached {

        @Test
        @DisplayName("should run onEmit")
        void shouldRunOnEmit() {
            TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mock(TriConsumer.class);
            EventProvider provider = new TestEventsProvider(ProviderState.READY);
            provider.attach(onEmit);
            EventDetails details = EventDetails.builder().build();
            provider.emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
            provider.emitProviderReady(details);
            provider.emitProviderConfigurationChanged(details);
            provider.emitProviderStale(details);
            provider.emitProviderError(details);
            verify(onEmit, times(1)).accept(provider, ProviderEvent.PROVIDER_READY, details);
            verify(onEmit, times(2)).accept(provider, ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
            verify(onEmit, times(1)).accept(provider, ProviderEvent.PROVIDER_STALE, details);
            verify(onEmit, times(1)).accept(provider, ProviderEvent.PROVIDER_ERROR, details);
        }
    }
}
package dev.openfeature.sdk;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * The details of a particular event.
 */
@Data
@SuperBuilder(toBuilder = true)
public class EventDetails extends ProviderEventDetails {
    private String clientName;
    private String providerName;

    static EventDetails fromProviderEventDetails(ProviderEventDetails providerEventDetails, String providerName) {
        return EventDetails.fromProviderEventDetails(providerEventDetails, providerName, null);
    }

    static EventDetails fromProviderEventDetails(
            ProviderEventDetails providerEventDetails,
            String providerName,
            String clientName) {
        return EventDetails.builder()
                .clientName(clientName)
                .providerName(providerName)
                .flagsChanged(providerEventDetails.getFlagsChanged())
                .eventMetadata(providerEventDetails.getEventMetadata())
                .message(providerEventDetails.getMessage())
                .build();
    }
}

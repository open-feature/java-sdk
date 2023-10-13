package dev.openfeature.sdk;

import edu.umd.cs.findbugs.annotations.Nullable;
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
        return EventDetails.fromProviderEventDetails(providerEventDetails, null, null);
    }

    static EventDetails fromProviderEventDetails(
            ProviderEventDetails providerEventDetails,
            @Nullable String providerName,
            @Nullable String clientName) {
        return EventDetails.builder()
                .clientName(clientName)
                .providerName(providerName)
                .flagsChanged(providerEventDetails.getFlagsChanged())
                .eventMetadata(providerEventDetails.getEventMetadata())
                .message(providerEventDetails.getMessage())
                .build();
    }
}

package dev.openfeature.sdk;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * The details of a particular event.
 */
@Data @SuperBuilder(toBuilder = true)
public class EventDetails extends ProviderEventDetails {
    private String clientName;

    static EventDetails fromProviderEventDetails(ProviderEventDetails providerEventDetails) {
        return EventDetails.fromProviderEventDetails(providerEventDetails, null);
    }

    static EventDetails fromProviderEventDetails(
            ProviderEventDetails providerEventDetails,
            @Nullable String clientName) {
        return EventDetails.builder()
                .clientName(clientName)
                .flagsChanged(providerEventDetails.getFlagsChanged())
                .eventMetadata(providerEventDetails.getEventMetadata())
                .message(providerEventDetails.getMessage())
                .build();
    }
}

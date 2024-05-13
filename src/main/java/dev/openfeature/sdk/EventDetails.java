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
    @Deprecated
    private String clientName;
    private String domain;
    private String providerName;

    static EventDetails fromProviderEventDetails(ProviderEventDetails providerEventDetails, String providerName) {
        return EventDetails.fromProviderEventDetails(providerEventDetails, providerName, null);
    }

    static EventDetails fromProviderEventDetails(
            ProviderEventDetails providerEventDetails,
            @Nullable String providerName,
            @Nullable String domain) {
        return EventDetails.builder()
                .domain(domain)
                .providerName(providerName)
                .flagsChanged(providerEventDetails.getFlagsChanged())
                .eventMetadata(providerEventDetails.getEventMetadata())
                .message(providerEventDetails.getMessage())
                .build();
    }
}

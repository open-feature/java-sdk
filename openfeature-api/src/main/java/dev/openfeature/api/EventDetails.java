package dev.openfeature.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * The details of a particular event.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class EventDetails extends ProviderEventDetails {
    private String domain;
    private String providerName;

    public static EventDetails fromProviderEventDetails(ProviderEventDetails providerEventDetails, String providerName) {
        return fromProviderEventDetails(providerEventDetails, providerName, null);
    }

    public static EventDetails fromProviderEventDetails(
            ProviderEventDetails providerEventDetails, String providerName, String domain) {
        return builder()
                .domain(domain)
                .providerName(providerName)
                .flagsChanged(providerEventDetails.getFlagsChanged())
                .eventMetadata(providerEventDetails.getEventMetadata())
                .message(providerEventDetails.getMessage())
                .build();
    }
}

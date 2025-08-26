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
    /** The domain associated with this event. */
    private String domain;

    /** The name of the provider that generated this event. */
    private String providerName;

    /**
     * Create EventDetails from ProviderEventDetails with provider name.
     *
     * @param providerEventDetails the provider event details
     * @param providerName the name of the provider
     * @return EventDetails instance
     */
    public static EventDetails fromProviderEventDetails(
            ProviderEventDetails providerEventDetails, String providerName) {
        return fromProviderEventDetails(providerEventDetails, providerName, null);
    }

    /**
     * Create EventDetails from ProviderEventDetails with provider name and domain.
     *
     * @param providerEventDetails the provider event details
     * @param providerName the name of the provider
     * @param domain the domain associated with the event
     * @return EventDetails instance
     */
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

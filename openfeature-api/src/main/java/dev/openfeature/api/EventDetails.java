package dev.openfeature.api;

import java.util.Objects;

/**
 * The details of a particular event.
 */
public class EventDetails extends ProviderEventDetails {
    /** The domain associated with this event. */
    private String domain;

    /** The name of the provider that generated this event. */
    private String providerName;

    public EventDetails() {
        super();
    }

    /**
     * Constructs an EventDetails with the specified domain and provider name.
     *
     * @param domain the domain associated with this event
     * @param providerName the name of the provider that generated this event
     */
    public EventDetails(String domain, String providerName) {
        super();
        this.domain = domain;
        this.providerName = providerName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public static EventDetailsBuilder eventDetailsBuilder() {
        return new EventDetailsBuilder();
    }

    /**
     * Returns a builder initialized with the current state of this object.
     *
     * @return a builder for EventDetails
     */
    public EventDetailsBuilder eventDetailsToBuilder() {
        return new EventDetailsBuilder()
                .domain(this.domain)
                .providerName(this.providerName)
                .flagsChanged(this.getFlagsChanged())
                .message(this.getMessage())
                .eventMetadata(this.getEventMetadata())
                .errorCode(this.getErrorCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        EventDetails that = (EventDetails) obj;
        return Objects.equals(domain, that.domain) && Objects.equals(providerName, that.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), domain, providerName);
    }

    @Override
    public String toString() {
        return "EventDetails{" + "domain='"
                + domain + '\'' + ", providerName='"
                + providerName + '\'' + ", flagsChanged="
                + getFlagsChanged() + ", message='"
                + getMessage() + '\'' + ", eventMetadata="
                + getEventMetadata() + ", errorCode="
                + getErrorCode() + '}';
    }

    /**
     * Builder class for creating instances of EventDetails.
     */
    public static class EventDetailsBuilder {
        private String domain;
        private String providerName;
        private java.util.List<String> flagsChanged;
        private String message;
        private ImmutableMetadata eventMetadata;
        private ErrorCode errorCode;

        public EventDetailsBuilder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public EventDetailsBuilder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public EventDetailsBuilder flagsChanged(java.util.List<String> flagsChanged) {
            this.flagsChanged = flagsChanged != null ? new java.util.ArrayList<>(flagsChanged) : null;
            return this;
        }

        public EventDetailsBuilder message(String message) {
            this.message = message;
            return this;
        }

        public EventDetailsBuilder eventMetadata(ImmutableMetadata eventMetadata) {
            this.eventMetadata = eventMetadata;
            return this;
        }

        public EventDetailsBuilder errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        /**
         * Builds an EventDetails instance with the configured parameters.
         *
         * @return a new EventDetails instance
         */
        public EventDetails build() {
            EventDetails eventDetails = new EventDetails(domain, providerName);
            eventDetails.setFlagsChanged(flagsChanged);
            eventDetails.setMessage(message);
            eventDetails.setEventMetadata(eventMetadata);
            eventDetails.setErrorCode(errorCode);
            return eventDetails;
        }
    }

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
        return eventDetailsBuilder()
                .domain(domain)
                .providerName(providerName)
                .flagsChanged(providerEventDetails.getFlagsChanged())
                .eventMetadata(providerEventDetails.getEventMetadata())
                .message(providerEventDetails.getMessage())
                .build();
    }
}

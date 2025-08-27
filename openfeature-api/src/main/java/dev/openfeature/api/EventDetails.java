package dev.openfeature.api;

import java.util.List;
import java.util.Objects;

/**
 * Event details delivered to event handlers, including provider context.
 * This represents the "event details" structure defined in the OpenFeature specification.
 * Contains all provider event details plus required provider identification.
 */
public class EventDetails implements EventDetailsInterface {
    /** The name of the provider that generated this event (required by OpenFeature spec). */
    private final String providerName;

    /** The domain associated with this event (may be null for global providers). */
    private final String domain;

    /** The provider event details containing the actual event information. */
    private final ProviderEventDetails providerEventDetails;

    /**
     * Constructs an EventDetails with the specified provider context and event details.
     *
     * @param providerName the name of the provider that generated this event (required)
     * @param domain the domain associated with this event (may be null)
     * @param providerEventDetails the provider event details (required)
     */
    private EventDetails(String providerName, String domain, ProviderEventDetails providerEventDetails) {
        this.providerName =
                Objects.requireNonNull(providerName, "providerName is required by OpenFeature specification");
        this.domain = domain;
        this.providerEventDetails = Objects.requireNonNull(providerEventDetails, "providerEventDetails cannot be null");
    }

    public String getProviderName() {
        return providerName;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * Gets the underlying provider event details.
     *
     * @return the provider event details
     */
    public ProviderEventDetails getProviderEventDetails() {
        return providerEventDetails;
    }

    // Delegation methods implementing EventDetailsInterface

    @Override
    public List<String> getFlagsChanged() {
        return providerEventDetails.getFlagsChanged();
    }

    @Override
    public String getMessage() {
        return providerEventDetails.getMessage();
    }

    @Override
    public ImmutableMetadata getEventMetadata() {
        return providerEventDetails.getEventMetadata();
    }

    @Override
    public ErrorCode getErrorCode() {
        return providerEventDetails.getErrorCode();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder initialized with the current state of this object.
     *
     * @return a builder for EventDetails
     */
    public Builder toBuilder() {
        return builder()
                .providerName(this.providerName)
                .domain(this.domain)
                .providerEventDetails(this.providerEventDetails);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EventDetails that = (EventDetails) obj;
        return Objects.equals(providerName, that.providerName)
                && Objects.equals(domain, that.domain)
                && Objects.equals(providerEventDetails, that.providerEventDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerName, domain, providerEventDetails);
    }

    @Override
    public String toString() {
        return "EventDetails{" + "providerName='"
                + providerName + '\'' + ", domain='"
                + domain + '\'' + ", providerEventDetails="
                + providerEventDetails + '}';
    }

    /**
     * Builder class for creating instances of EventDetails.
     */
    public static class Builder {
        private String providerName;
        private String domain;
        private ProviderEventDetails providerEventDetails;

        private Builder() {}

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder providerEventDetails(ProviderEventDetails providerEventDetails) {
            this.providerEventDetails = providerEventDetails;
            return this;
        }

        // Convenience methods for building provider event details inline
        /**
         * Sets the list of flags that changed.
         *
         * @param flagsChanged list of flag keys that changed
         * @return this builder
         */
        public Builder flagsChanged(List<String> flagsChanged) {
            ensureProviderEventDetailsBuilder();
            this.providerEventDetails = ProviderEventDetails.builder()
                    .flagsChanged(flagsChanged)
                    .message(getProviderEventDetailsOrEmpty().getMessage())
                    .eventMetadata(getProviderEventDetailsOrEmpty().getEventMetadata())
                    .errorCode(getProviderEventDetailsOrEmpty().getErrorCode())
                    .build();
            return this;
        }

        /**
         * Sets the message describing the event.
         *
         * @param message message describing the event (should be populated for PROVIDER_ERROR events)
         * @return this builder
         */
        public Builder message(String message) {
            ensureProviderEventDetailsBuilder();
            this.providerEventDetails = ProviderEventDetails.builder()
                    .flagsChanged(getProviderEventDetailsOrEmpty().getFlagsChanged())
                    .message(message)
                    .eventMetadata(getProviderEventDetailsOrEmpty().getEventMetadata())
                    .errorCode(getProviderEventDetailsOrEmpty().getErrorCode())
                    .build();
            return this;
        }

        /**
         * Sets the metadata associated with the event.
         *
         * @param eventMetadata metadata associated with the event
         * @return this builder
         */
        public Builder eventMetadata(ImmutableMetadata eventMetadata) {
            ensureProviderEventDetailsBuilder();
            this.providerEventDetails = ProviderEventDetails.builder()
                    .flagsChanged(getProviderEventDetailsOrEmpty().getFlagsChanged())
                    .message(getProviderEventDetailsOrEmpty().getMessage())
                    .eventMetadata(eventMetadata)
                    .errorCode(getProviderEventDetailsOrEmpty().getErrorCode())
                    .build();
            return this;
        }

        /**
         * Sets the error code for the event.
         *
         * @param errorCode error code (should be populated for PROVIDER_ERROR events)
         * @return this builder
         */
        public Builder errorCode(ErrorCode errorCode) {
            ensureProviderEventDetailsBuilder();
            this.providerEventDetails = ProviderEventDetails.builder()
                    .flagsChanged(getProviderEventDetailsOrEmpty().getFlagsChanged())
                    .message(getProviderEventDetailsOrEmpty().getMessage())
                    .eventMetadata(getProviderEventDetailsOrEmpty().getEventMetadata())
                    .errorCode(errorCode)
                    .build();
            return this;
        }

        private void ensureProviderEventDetailsBuilder() {
            if (this.providerEventDetails == null) {
                this.providerEventDetails = ProviderEventDetails.builder().build();
            }
        }

        private ProviderEventDetails getProviderEventDetailsOrEmpty() {
            return this.providerEventDetails != null
                    ? this.providerEventDetails
                    : ProviderEventDetails.builder().build();
        }

        /**
         * Builds an EventDetails instance with the configured parameters.
         *
         * @return a new EventDetails instance
         */
        public EventDetails build() {
            if (providerEventDetails == null) {
                providerEventDetails = ProviderEventDetails.builder().build();
            }
            return new EventDetails(providerName, domain, providerEventDetails);
        }
    }
}

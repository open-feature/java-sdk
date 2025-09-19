package dev.openfeature.api.events;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.types.Metadata;
import java.util.List;
import java.util.Objects;

/**
 * Event details delivered to event handlers, including provider context.
 * This represents the "event details" structure defined in the OpenFeature specification.
 * Contains all provider event details plus required provider identification.
 */
class DefaultEventDetails implements EventDetails {
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
    DefaultEventDetails(String providerName, String domain, ProviderEventDetails providerEventDetails) {
        this.providerName =
                Objects.requireNonNull(providerName, "providerName is required by OpenFeature specification");
        this.domain = domain;
        this.providerEventDetails = Objects.requireNonNull(providerEventDetails, "providerEventDetails cannot be null");
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
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
    public Metadata getEventMetadata() {
        return providerEventDetails.getEventMetadata();
    }

    @Override
    public ErrorCode getErrorCode() {
        return providerEventDetails.getErrorCode();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DefaultEventDetails that = (DefaultEventDetails) obj;
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

        /**
         * Builds an EventDetails instance with the configured parameters.
         *
         * @return a new EventDetails instance
         */
        public DefaultEventDetails build() {
            if (providerEventDetails == null) {
                providerEventDetails = ProviderEventDetails.EMPTY;
            }
            return new DefaultEventDetails(providerName, domain, providerEventDetails);
        }
    }
}

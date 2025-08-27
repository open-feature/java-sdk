package dev.openfeature.api;

import java.util.List;
import java.util.Objects;

/**
 * Details of a provider event, as emitted by providers.
 * This represents the "provider event details" structure defined in the OpenFeature specification.
 * Providers emit these events, which are then enriched by the SDK with provider context.
 */
public class ProviderEventDetails implements EventDetailsInterface {
    private final List<String> flagsChanged;
    private final String message;
    private final ImmutableMetadata eventMetadata;
    private final ErrorCode errorCode;

    /**
     * Creates an empty ProviderEventDetails for backwards compatibility.
     *
     * @deprecated Use builder() instead
     */
    @Deprecated
    private ProviderEventDetails() {
        this(null, null, null, null);
    }

    /**
     * Constructs a ProviderEventDetails with the specified parameters.
     *
     * @param flagsChanged list of flags that changed (may be null)
     * @param message message describing the event (should be populated for PROVIDER_ERROR events)
     * @param eventMetadata metadata associated with the event (may be null)
     * @param errorCode error code (should be populated for PROVIDER_ERROR events)
     */
    private ProviderEventDetails(
            List<String> flagsChanged, String message, ImmutableMetadata eventMetadata, ErrorCode errorCode) {
        this.flagsChanged = flagsChanged != null ? List.copyOf(flagsChanged) : null;
        this.message = message;
        this.eventMetadata = eventMetadata;
        this.errorCode = errorCode;
    }

    public List<String> getFlagsChanged() {
        return flagsChanged;
    }

    public String getMessage() {
        return message;
    }

    public ImmutableMetadata getEventMetadata() {
        return eventMetadata;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder initialized with the current state of this object.
     *
     * @return a builder for ProviderEventDetails
     */
    public Builder toBuilder() {
        return builder()
                .flagsChanged(this.flagsChanged)
                .message(this.message)
                .eventMetadata(this.eventMetadata)
                .errorCode(this.errorCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProviderEventDetails that = (ProviderEventDetails) obj;
        return Objects.equals(flagsChanged, that.flagsChanged)
                && Objects.equals(message, that.message)
                && Objects.equals(eventMetadata, that.eventMetadata)
                && errorCode == that.errorCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flagsChanged, message, eventMetadata, errorCode);
    }

    @Override
    public String toString() {
        return "ProviderEventDetails{" + "flagsChanged="
                + flagsChanged + ", message='"
                + message + '\'' + ", eventMetadata="
                + eventMetadata + ", errorCode="
                + errorCode + '}';
    }

    /**
     * Builder class for creating instances of ProviderEventDetails.
     */
    public static class Builder {
        private List<String> flagsChanged;
        private String message;
        private ImmutableMetadata eventMetadata;
        private ErrorCode errorCode;

        private Builder() {}

        public Builder flagsChanged(List<String> flagsChanged) {
            this.flagsChanged = flagsChanged != null ? List.copyOf(flagsChanged) : null;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder eventMetadata(ImmutableMetadata eventMetadata) {
            this.eventMetadata = eventMetadata;
            return this;
        }

        public Builder errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ProviderEventDetails build() {
            return new ProviderEventDetails(flagsChanged, message, eventMetadata, errorCode);
        }
    }
}

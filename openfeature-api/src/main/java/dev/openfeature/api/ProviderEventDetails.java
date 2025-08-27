package dev.openfeature.api;

import java.util.List;
import java.util.Objects;

/**
 * The details of a particular event.
 */
public class ProviderEventDetails {
    private List<String> flagsChanged;
    private String message;
    private ImmutableMetadata eventMetadata;
    private ErrorCode errorCode;

    public ProviderEventDetails() {}

    /**
     * Constructs a ProviderEventDetails with the specified parameters.
     *
     * @param flagsChanged list of flags that changed
     * @param message message describing the event
     * @param eventMetadata metadata associated with the event
     * @param errorCode error code if applicable
     */
    public ProviderEventDetails(
            List<String> flagsChanged, String message, ImmutableMetadata eventMetadata, ErrorCode errorCode) {
        this.flagsChanged = flagsChanged;
        this.message = message;
        this.eventMetadata = eventMetadata;
        this.errorCode = errorCode;
    }

    public List<String> getFlagsChanged() {
        return flagsChanged;
    }

    public void setFlagsChanged(List<String> flagsChanged) {
        this.flagsChanged = flagsChanged;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ImmutableMetadata getEventMetadata() {
        return eventMetadata;
    }

    public void setEventMetadata(ImmutableMetadata eventMetadata) {
        this.eventMetadata = eventMetadata;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public static ProviderEventDetailsBuilder builder() {
        return new ProviderEventDetailsBuilder();
    }

    /**
     * Returns a builder initialized with the current state of this object.
     *
     * @return a builder for ProviderEventDetails
     */
    public ProviderEventDetailsBuilder toBuilder() {
        return new ProviderEventDetailsBuilder()
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
    public static class ProviderEventDetailsBuilder {
        private List<String> flagsChanged;
        private String message;
        private ImmutableMetadata eventMetadata;
        private ErrorCode errorCode;

        public ProviderEventDetailsBuilder flagsChanged(List<String> flagsChanged) {
            this.flagsChanged = flagsChanged;
            return this;
        }

        public ProviderEventDetailsBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ProviderEventDetailsBuilder eventMetadata(ImmutableMetadata eventMetadata) {
            this.eventMetadata = eventMetadata;
            return this;
        }

        public ProviderEventDetailsBuilder errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ProviderEventDetails build() {
            return new ProviderEventDetails(flagsChanged, message, eventMetadata, errorCode);
        }
    }
}

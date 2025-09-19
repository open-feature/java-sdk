package dev.openfeature.api.events;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.types.Metadata;
import java.util.List;
import java.util.Objects;

/**
 * Details of a provider event, as emitted by providers.
 * This represents the "provider event details" structure defined in the OpenFeature specification.
 * Providers emit these events, which are then enriched by the SDK with provider context.
 */
class DefaultProviderEventDetails implements ProviderEventDetails {
    private final List<String> flagsChanged;
    private final String message;
    private final Metadata eventMetadata;
    private final ErrorCode errorCode;

    /**
     * Creates an empty ProviderEventDetails for backwards compatibility.
     */
    DefaultProviderEventDetails() {
        this(null, null, null, null);
    }

    /**
     * Constructs a ProviderEventDetails with the specified parameters.
     *
     * @param flagsChanged  list of flags that changed (may be null)
     * @param message       message describing the event (should be populated for PROVIDER_ERROR events)
     * @param eventMetadata metadata associated with the event (may be null)
     * @param errorCode     error code (should be populated for PROVIDER_ERROR events)
     */
    DefaultProviderEventDetails(
            List<String> flagsChanged, String message, Metadata eventMetadata, ErrorCode errorCode) {
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

    public Metadata getEventMetadata() {
        return eventMetadata;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DefaultProviderEventDetails that = (DefaultProviderEventDetails) obj;
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
}

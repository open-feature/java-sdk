package dev.openfeature.api.events;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.types.Metadata;
import java.util.List;

/**
 * Common interface for event details providing access to event information.
 * This interface defines the common methods available on both ProviderEventDetails
 * and EventDetails, ensuring consistent access patterns.
 */
public interface ProviderEventDetails {

    ProviderEventDetails EMPTY = new DefaultProviderEventDetails();

    static ProviderEventDetails of(String message) {
        return of(message, null);
    }

    static ProviderEventDetails of(ErrorCode errorCode) {
        return of(null, null, null, errorCode);
    }

    static ProviderEventDetails of(String message, List<String> flagsChanged) {
        return of(message, flagsChanged, null);
    }

    static ProviderEventDetails of(String message, List<String> flagsChanged, Metadata metadata) {
        return of(message, flagsChanged, metadata, null);
    }

    static ProviderEventDetails of(String message, List<String> flags, Metadata metadata, ErrorCode errorCode) {
        return new DefaultProviderEventDetails(flags, message, metadata, errorCode);
    }

    /**
     * Gets the list of flag keys that changed in this event.
     *
     * @return list of changed flag keys, or null if not applicable
     */
    List<String> getFlagsChanged();

    /**
     * Gets the message associated with this event.
     * For PROVIDER_ERROR events, this should contain the error message.
     *
     * @return event message, or null if none
     */
    String getMessage();

    /**
     * Gets the metadata associated with this event.
     *
     * @return event metadata, or null if none
     */
    Metadata getEventMetadata();

    /**
     * Gets the error code associated with this event.
     * For PROVIDER_ERROR events, this should contain the error code.
     *
     * @return error code, or null if not applicable
     */
    ErrorCode getErrorCode();
}

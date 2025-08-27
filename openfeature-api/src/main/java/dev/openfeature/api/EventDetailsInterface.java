package dev.openfeature.api;

import java.util.List;

/**
 * Common interface for event details providing access to event information.
 * This interface defines the common methods available on both ProviderEventDetails
 * and EventDetails, ensuring consistent access patterns.
 */
public interface EventDetailsInterface {

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
    ImmutableMetadata getEventMetadata();

    /**
     * Gets the error code associated with this event.
     * For PROVIDER_ERROR events, this should contain the error code.
     *
     * @return error code, or null if not applicable
     */
    ErrorCode getErrorCode();
}

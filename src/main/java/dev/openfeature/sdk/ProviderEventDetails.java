package dev.openfeature.sdk;

import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * The details of a particular event.
 */
@Data
@SuperBuilder(toBuilder = true)
public class ProviderEventDetails {
    private List<String> flagsChanged;
    private String message;
    private ImmutableMetadata eventMetadata;
    private ErrorCode errorCode;
}

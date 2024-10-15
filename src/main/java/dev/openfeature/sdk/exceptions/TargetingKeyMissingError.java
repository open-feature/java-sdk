package dev.openfeature.sdk.exceptions;

import dev.openfeature.sdk.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

/**
 * The provider requires a targeting key and one was not provided in the evaluation context.
 */
@StandardException
public class TargetingKeyMissingError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Getter
    private final ErrorCode errorCode = ErrorCode.TARGETING_KEY_MISSING;

}

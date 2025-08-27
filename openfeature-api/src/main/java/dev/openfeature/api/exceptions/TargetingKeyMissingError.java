package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

/**
 * The provider requires a targeting key and one was not provided in the evaluation context.
 */
public class TargetingKeyMissingError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.TARGETING_KEY_MISSING;

    public TargetingKeyMissingError() {
        super();
    }

    public TargetingKeyMissingError(String message) {
        super(message);
    }

    public TargetingKeyMissingError(String message, Throwable cause) {
        super(message, cause);
    }

    public TargetingKeyMissingError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

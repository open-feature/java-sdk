package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

/**
 * The evaluation context does not meet provider requirements.
 */
public class InvalidContextError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.INVALID_CONTEXT;

    public InvalidContextError() {
        super();
    }

    public InvalidContextError(String message) {
        super(message);
    }

    public InvalidContextError(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidContextError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

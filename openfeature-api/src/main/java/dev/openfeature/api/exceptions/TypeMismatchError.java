package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

/**
 * The type of the flag value does not match the expected type.
 */
@SuppressWarnings({"checkstyle:MissingJavadocType", "squid:S110"})
public class TypeMismatchError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.TYPE_MISMATCH;

    public TypeMismatchError() {
        super();
    }

    public TypeMismatchError(String message) {
        super(message);
    }

    public TypeMismatchError(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeMismatchError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

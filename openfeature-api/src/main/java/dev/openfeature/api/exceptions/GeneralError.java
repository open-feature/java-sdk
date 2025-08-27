package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class GeneralError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.GENERAL;

    public GeneralError() {
        super();
    }

    public GeneralError(String message) {
        super(message);
    }

    public GeneralError(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneralError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class FatalError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.PROVIDER_FATAL;

    public FatalError() {
        super();
    }

    public FatalError(String message) {
        super(message);
    }

    public FatalError(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

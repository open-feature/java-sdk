package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

@SuppressWarnings({"checkstyle:MissingJavadocType", "squid:S110"})
public class FlagNotFoundError extends OpenFeatureErrorWithoutStacktrace {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.FLAG_NOT_FOUND;

    public FlagNotFoundError() {
        super();
    }

    public FlagNotFoundError(String message) {
        super(message);
    }

    public FlagNotFoundError(String message, Throwable cause) {
        super(message, cause);
    }

    public FlagNotFoundError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

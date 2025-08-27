package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

@SuppressWarnings("checkstyle:MissingJavadocType")
public abstract class OpenFeatureError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OpenFeatureError() {
        super();
    }

    public OpenFeatureError(String message) {
        super(message);
    }

    public OpenFeatureError(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenFeatureError(Throwable cause) {
        super(cause);
    }

    public abstract ErrorCode getErrorCode();
}

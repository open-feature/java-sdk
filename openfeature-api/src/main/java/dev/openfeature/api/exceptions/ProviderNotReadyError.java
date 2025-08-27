package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

@SuppressWarnings({"checkstyle:MissingJavadocType", "squid:S110"})
public class ProviderNotReadyError extends OpenFeatureErrorWithoutStacktrace {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;

    public ProviderNotReadyError() {
        super();
    }

    public ProviderNotReadyError(String message) {
        super(message);
    }

    public ProviderNotReadyError(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderNotReadyError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

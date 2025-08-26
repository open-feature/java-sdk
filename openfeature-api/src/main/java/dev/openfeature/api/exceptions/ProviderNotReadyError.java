package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

@SuppressWarnings({"checkstyle:MissingJavadocType", "squid:S110"})
@StandardException
public class ProviderNotReadyError extends OpenFeatureErrorWithoutStacktrace {
    private static final long serialVersionUID = 1L;

    @Getter
    private final ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
}

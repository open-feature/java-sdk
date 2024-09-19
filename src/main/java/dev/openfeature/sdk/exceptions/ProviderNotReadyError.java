package dev.openfeature.sdk.exceptions;

import dev.openfeature.sdk.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

@SuppressWarnings({"checkstyle:MissingJavadocType", "squid:S110"})
@StandardException
public class ProviderNotReadyError extends OpenFeatureErrorWithoutStacktrace {
    private static final long serialVersionUID = 1L;
    @Getter private final ErrorCode errorCode = ErrorCode.PROVIDER_NOT_READY;
}

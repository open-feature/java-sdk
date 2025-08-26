package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

@SuppressWarnings("checkstyle:MissingJavadocType")
@StandardException
public class FatalError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Getter
    private final ErrorCode errorCode = ErrorCode.PROVIDER_FATAL;
}

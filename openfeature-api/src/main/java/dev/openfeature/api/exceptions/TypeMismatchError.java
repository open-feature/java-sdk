package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

/**
 * The type of the flag value does not match the expected type.
 */
@SuppressWarnings({"checkstyle:MissingJavadocType", "squid:S110"})
@StandardException
public class TypeMismatchError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Getter
    private final ErrorCode errorCode = ErrorCode.TYPE_MISMATCH;
}

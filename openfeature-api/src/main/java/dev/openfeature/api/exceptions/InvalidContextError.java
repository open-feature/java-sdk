package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

/**
 * The evaluation context does not meet provider requirements.
 */
@StandardException
public class InvalidContextError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Getter
    private final ErrorCode errorCode = ErrorCode.INVALID_CONTEXT;
}

package dev.openfeature.sdk.exceptions;

import dev.openfeature.sdk.ErrorCode;
import lombok.experimental.StandardException;

@StandardException
public abstract class OpenFeatureError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public abstract ErrorCode getErrorCode();
}

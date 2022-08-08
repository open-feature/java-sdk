package dev.openfeature.javasdk.exceptions;

import dev.openfeature.javasdk.ErrorCode;
import lombok.experimental.StandardException;

@StandardException
public abstract class OpenFeatureError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public abstract ErrorCode getErrorCode();
}

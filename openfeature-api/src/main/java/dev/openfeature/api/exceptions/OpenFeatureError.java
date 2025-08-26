package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.experimental.StandardException;

@SuppressWarnings("checkstyle:MissingJavadocType")
@StandardException
public abstract class OpenFeatureError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public abstract ErrorCode getErrorCode();
}

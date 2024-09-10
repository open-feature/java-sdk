package dev.openfeature.sdk.exceptions;

import lombok.experimental.StandardException;

@SuppressWarnings("checkstyle:MissingJavadocType")
@StandardException
public abstract class OpenFeatureErrorWithoutStacktrace extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

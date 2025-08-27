package dev.openfeature.api.exceptions;

@SuppressWarnings("checkstyle:MissingJavadocType")
public abstract class OpenFeatureErrorWithoutStacktrace extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    public OpenFeatureErrorWithoutStacktrace() {
        super();
    }

    public OpenFeatureErrorWithoutStacktrace(String message) {
        super(message);
    }

    public OpenFeatureErrorWithoutStacktrace(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenFeatureErrorWithoutStacktrace(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

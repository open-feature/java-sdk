package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.types.Value;

/**
 * The value can not be converted to a {@link Value}.
 */
public class ValueNotConvertableError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.GENERAL;

    public ValueNotConvertableError() {
        super();
    }

    public ValueNotConvertableError(String message) {
        super(message);
    }

    public ValueNotConvertableError(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueNotConvertableError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

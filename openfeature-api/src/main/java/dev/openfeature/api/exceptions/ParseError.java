package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

/**
 * An error was encountered parsing data, such as a flag configuration.
 */
public class ParseError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode = ErrorCode.PARSE_ERROR;

    public ParseError() {
        super();
    }

    public ParseError(String message) {
        super(message);
    }

    public ParseError(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseError(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

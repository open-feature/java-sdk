package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

/**
 * An error was encountered parsing data, such as a flag configuration.
 */
@StandardException
public class ParseError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Getter
    private final ErrorCode errorCode = ErrorCode.PARSE_ERROR;
}

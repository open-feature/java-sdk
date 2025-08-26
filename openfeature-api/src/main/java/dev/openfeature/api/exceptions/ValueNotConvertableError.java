package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

/**
 * The value can not be converted to a {@link dev.openfeature.api.Value}.
 */
@StandardException
public class ValueNotConvertableError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Getter
    private final ErrorCode errorCode = ErrorCode.GENERAL;
}

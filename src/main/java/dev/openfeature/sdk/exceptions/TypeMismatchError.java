package dev.openfeature.sdk.exceptions;

import dev.openfeature.sdk.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

@StandardException
public class TypeMismatchError extends OpenFeatureError {
    private static final long serialVersionUID = 1L;

    @Getter private final ErrorCode errorCode = ErrorCode.TYPE_MISMATCH;

}

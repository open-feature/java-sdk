package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

@StandardException
public class TypeMismatchError extends OpenFeatureError {
    private static long serialVersionUID = 1L;

    @Getter private final ErrorCode errorCode = ErrorCode.TYPE_MISMATCH;

}

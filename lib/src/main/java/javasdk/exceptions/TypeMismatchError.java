package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.Getter;

public class TypeMismatchError extends OpenFeatureError {
    @Getter
    private final ErrorCode errorCode = ErrorCode.TYPE_MISMATCH;

}

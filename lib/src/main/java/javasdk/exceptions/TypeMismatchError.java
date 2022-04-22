package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.Getter;

public class TypeMismatchError extends OpenFeatureError {
    @Getter
    private ErrorCode errorCode = ErrorCode.TYPE_MISMATCH;

}

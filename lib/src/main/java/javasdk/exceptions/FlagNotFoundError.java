package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.Getter;
import lombok.experimental.StandardException;

@StandardException
public class FlagNotFoundError extends OpenFeatureError {
    private static long serialVersionUID = 1L;
    @Getter private final ErrorCode errorCode = ErrorCode.GENERAL;
}

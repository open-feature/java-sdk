package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.experimental.StandardException;

@StandardException
public abstract class OpenFeatureError extends RuntimeException {
    private static long serialVersionUID = 1L;
    public abstract ErrorCode getErrorCode();
}

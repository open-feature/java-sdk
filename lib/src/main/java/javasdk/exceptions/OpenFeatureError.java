package javasdk.exceptions;

import javasdk.ErrorCode;

public abstract class OpenFeatureError extends RuntimeException {
    public abstract ErrorCode getErrorCode();
}

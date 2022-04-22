package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.Getter;

public class GeneralError extends OpenFeatureError{
    @Getter private ErrorCode errorCode = ErrorCode.GENERAL;
    private final String message;

    public GeneralError(String message) {
        this.message = message;
    }
}

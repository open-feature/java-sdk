package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.Getter;

public class FlagNotFoundError extends OpenFeatureError {
    @Getter private ErrorCode errorCode = ErrorCode.GENERAL;
}

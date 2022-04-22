package javasdk.exceptions;

import javasdk.ErrorCode;
import lombok.Getter;

public class ParseError extends OpenFeatureError {
    @Getter
    private ErrorCode errorCode = ErrorCode.PARSE_ERROR;

}

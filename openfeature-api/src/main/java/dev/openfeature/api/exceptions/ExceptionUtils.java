package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;
import lombok.experimental.UtilityClass;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class ExceptionUtils {

    /**
     * Creates an Error for the specific error code.
     *
     * @param errorCode    the ErrorCode to use
     * @param errorMessage the error message to include in the returned error
     * @return the specific OpenFeatureError for the errorCode
     */
    public static OpenFeatureError instantiateErrorByErrorCode(ErrorCode errorCode, String errorMessage) {
        switch (errorCode) {
            case FLAG_NOT_FOUND:
                return new FlagNotFoundError(errorMessage);
            case PARSE_ERROR:
                return new ParseError(errorMessage);
            case TYPE_MISMATCH:
                return new TypeMismatchError(errorMessage);
            case TARGETING_KEY_MISSING:
                return new TargetingKeyMissingError(errorMessage);
            case INVALID_CONTEXT:
                return new InvalidContextError(errorMessage);
            case PROVIDER_NOT_READY:
                return new ProviderNotReadyError(errorMessage);
            default:
                return new GeneralError(errorMessage);
        }
    }
}

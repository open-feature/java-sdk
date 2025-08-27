package dev.openfeature.api.exceptions;

import dev.openfeature.api.ErrorCode;

@SuppressWarnings("checkstyle:MissingJavadocType")
public final class ExceptionUtils {

    private ExceptionUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

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

package dev.openfeature.sdk.internal;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.InvalidContextError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.exceptions.ParseError;
import dev.openfeature.sdk.exceptions.ProviderNotReadyError;
import dev.openfeature.sdk.exceptions.TargetingKeyMissingError;
import dev.openfeature.sdk.exceptions.TypeMismatchError;
import lombok.experimental.UtilityClass;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class ErrorUtils {

    /**
     * Creates an Error for the specific error code.
     * @param errorCode the ErrorCode to use
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

package dev.openfeature.sdk.exceptions;

import dev.openfeature.api.exceptions.GeneralError as ApiGeneralError;

/**
 * @deprecated Use {@link dev.openfeature.api.exceptions.GeneralError} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.exceptions.GeneralError;
 * throw new GeneralError("error message");
 *
 * // After
 * import dev.openfeature.api.exceptions.GeneralError;
 * throw new GeneralError("error message");
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class GeneralError extends ApiGeneralError {

    /**
     * @deprecated Use {@link dev.openfeature.api.exceptions.GeneralError#GeneralError(String)} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public GeneralError(String message) {
        super(message);
    }
}
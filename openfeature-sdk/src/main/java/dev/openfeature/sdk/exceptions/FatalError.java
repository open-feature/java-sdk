package dev.openfeature.sdk.exceptions;

import dev.openfeature.api.exceptions.FatalError as ApiFatalError;

/**
 * @deprecated Use {@link dev.openfeature.api.exceptions.FatalError} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.exceptions.FatalError;
 * throw new FatalError("error message");
 *
 * // After
 * import dev.openfeature.api.exceptions.FatalError;
 * throw new FatalError("error message");
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class FatalError extends ApiFatalError {

    /**
     * @deprecated Use {@link dev.openfeature.api.exceptions.FatalError#FatalError(String)} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public FatalError(String message) {
        super(message);
    }
}
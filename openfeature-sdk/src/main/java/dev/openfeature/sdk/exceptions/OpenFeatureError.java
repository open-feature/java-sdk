package dev.openfeature.sdk.exceptions;

import dev.openfeature.api.exceptions.OpenFeatureError as ApiOpenFeatureError;

/**
 * @deprecated Use {@link dev.openfeature.api.exceptions.OpenFeatureError} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.exceptions.OpenFeatureError;
 * throw new OpenFeatureError("error message");
 *
 * // After
 * import dev.openfeature.api.exceptions.OpenFeatureError;
 * throw new OpenFeatureError("error message");
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class OpenFeatureError extends ApiOpenFeatureError {

    /**
     * @deprecated Use {@link dev.openfeature.api.exceptions.OpenFeatureError#OpenFeatureError(String)} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public OpenFeatureError(String message) {
        super(message);
    }
}
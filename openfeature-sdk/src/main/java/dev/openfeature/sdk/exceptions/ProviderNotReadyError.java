package dev.openfeature.sdk.exceptions;

import dev.openfeature.api.exceptions.ProviderNotReadyError as ApiProviderNotReadyError;

/**
 * @deprecated Use {@link dev.openfeature.api.exceptions.ProviderNotReadyError} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.exceptions.ProviderNotReadyError;
 * throw new ProviderNotReadyError("error message");
 *
 * // After
 * import dev.openfeature.api.exceptions.ProviderNotReadyError;
 * throw new ProviderNotReadyError("error message");
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class ProviderNotReadyError extends ApiProviderNotReadyError {

    /**
     * @deprecated Use {@link dev.openfeature.api.exceptions.ProviderNotReadyError#ProviderNotReadyError(String)} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public ProviderNotReadyError(String message) {
        super(message);
    }
}
package dev.openfeature.sdk;

// Note: Java doesn't support import aliases, so we use the fully qualified name

/**
 * @deprecated Use {@link dev.openfeature.api.Client} instead.
 * This interface will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.Client;
 * Client client = OpenFeature.getClient();
 *
 * // After
 * import dev.openfeature.api.Client;
 * Client client = OpenFeature.getClient();
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@SuppressWarnings("deprecation")
public interface Client extends dev.openfeature.api.Client {
    // This interface now extends the new Client interface
    // All existing usage will continue to work
    // but should migrate to dev.openfeature.api.Client
}
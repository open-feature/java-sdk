package dev.openfeature.api;

/**
 * Metadata specific to an OpenFeature {@code Client}.
 */
public interface ClientMetadata {
    String getDomain();

    @Deprecated
    // this is here for compatibility with getName() exposed from {@link Metadata}
    default String getName() {
        return getDomain();
    }
}

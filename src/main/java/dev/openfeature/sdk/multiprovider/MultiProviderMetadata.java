package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.Metadata;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Metadata for {@link MultiProvider}.
 *
 * <p>Contains the multiprovider's own name and a map of the original metadata from each underlying
 * provider.
 */
@Value
@Builder
public class MultiProviderMetadata implements Metadata {

    String name;
    Map<String, Metadata> originalMetadata;
}

package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.Metadata;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Metadata for {@link MultiProvider}.
 *
 * <p>Contains the multiprovider's own name and a list of the original metadata from each underlying
 * provider.
 */
@Value
@Builder
public class MultiProviderMetadata implements Metadata {

    String name;
    List<Metadata> originalMetadata;
}

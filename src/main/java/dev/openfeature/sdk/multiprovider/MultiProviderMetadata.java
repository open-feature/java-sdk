package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.Metadata;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Metadata class for Multiprovider.
 */
@Data
@Builder
public class MultiProviderMetadata implements Metadata {
    String name;
    Map<String, Metadata> originalMetadata;

    @Override
    public String getName() {
        return name;
    }
}

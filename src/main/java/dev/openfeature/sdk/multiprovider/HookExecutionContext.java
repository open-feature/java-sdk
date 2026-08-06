package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ClientMetadata;
import java.util.Map;

/** Captures hook lifecycle context (client metadata and hints) for per-provider hook execution. */
final class HookExecutionContext {
    final ClientMetadata clientMetadata;
    final Map<String, Object> hints;

    HookExecutionContext(ClientMetadata clientMetadata, Map<String, Object> hints) {
        this.clientMetadata = clientMetadata;
        this.hints = hints;
    }
}

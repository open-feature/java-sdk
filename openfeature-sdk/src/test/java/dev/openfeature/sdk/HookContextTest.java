package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import dev.openfeature.api.*;
import dev.openfeature.api.ProviderMetadata;
import org.junit.jupiter.api.Test;

class HookContextTest {
    @Specification(
            number = "4.2.2.2",
            text = "Condition: The client metadata field in the hook context MUST be immutable.")
    @Specification(
            number = "4.2.2.3",
            text = "Condition: The provider metadata field in the hook context MUST be immutable.")
    @Test
    void metadata_field_is_type_metadata() {
        ClientMetadata clientMetadata = mock(ClientMetadata.class);
        ProviderMetadata meta = mock(ProviderMetadata.class);
        HookContext<Boolean> hc = HookContext.<Boolean>builder()
                .flagKey("key")
                .type(FlagValueType.BOOLEAN)
                .clientMetadata(clientMetadata)
                .providerMetadata(meta)
                .ctx(EvaluationContext.EMPTY)
                .defaultValue(false)
                .build();

        assertTrue(ClientMetadata.class.isAssignableFrom(hc.getClientMetadata().getClass()));
        assertTrue(
                ProviderMetadata.class.isAssignableFrom(hc.getProviderMetadata().getClass()));
    }

    @Specification(
            number = "4.3.3.1",
            text =
                    "The before stage MUST run before flag resolution occurs. It accepts a hook context (required) and hook hints (optional) as parameters. It has no return value.")
    @Test
    void not_applicable_for_dynamic_context() {}
}

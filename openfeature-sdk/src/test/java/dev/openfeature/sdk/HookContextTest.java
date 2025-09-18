package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
        Metadata meta = mock(Metadata.class);
        HookContext<Object> hc =
                HookContext.from("key", FlagValueType.BOOLEAN, clientMetadata, meta, new ImmutableContext(), false);

        assertTrue(ClientMetadata.class.isAssignableFrom(hc.getClientMetadata().getClass()));
        assertTrue(Metadata.class.isAssignableFrom(hc.getProviderMetadata().getClass()));
    }

    @Specification(
            number = "4.3.3.1",
            text =
                    "The before stage MUST run before flag resolution occurs. It accepts a hook context (required) and hook hints (optional) as parameters. It has no return value.")
    @Test
    void not_applicable_for_dynamic_context() {}
}

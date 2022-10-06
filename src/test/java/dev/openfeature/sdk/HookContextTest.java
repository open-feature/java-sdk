package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class HookContextTest {
    @Specification(number="4.2.2.2", text="Condition: The client metadata field in the hook context MUST be immutable.")
    @Specification(number="4.2.2.3", text="Condition: The provider metadata field in the hook context MUST be immutable.")
    @Test void metadata_field_is_type_metadata() {
        Metadata meta = mock(Metadata.class);
        HookContext<Object> hc = HookContext.from(
                "key",
                FlagValueType.BOOLEAN,
                meta,
                meta,
                new MutableContext(),
                false
        );

        assertTrue(Metadata.class.isAssignableFrom(hc.getClientMetadata().getClass()));
        assertTrue(Metadata.class.isAssignableFrom(hc.getProviderMetadata().getClass()));
    }

}
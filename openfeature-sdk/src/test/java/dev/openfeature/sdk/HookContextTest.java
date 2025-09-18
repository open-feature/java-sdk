package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import dev.openfeature.api.*;
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
        HookContext<Object> hc = HookContextWithoutData.of("key", FlagValueType.BOOLEAN, clientMetadata, meta, false);

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

    @Test
    void shouldCreateHookContextWithHookData() {
        HookData hookData = HookData.create();
        hookData.set("test", "value");

        HookContextWithData context = HookContextWithData.of(mock(HookContext.class), hookData);

        assertNotNull(context.getHookData());
        assertEquals("value", context.getHookData().get("test"));
    }

    @Test
    void shouldCreateHookContextWithoutHookData() {
        HookContext<String> context = HookContextWithoutData.of("test-flag", FlagValueType.STRING, "default");

        assertNull(context.getHookData());
    }

    @Test
    void shouldCreateHookContextWithHookDataUsingWith() {
        HookContext<String> originalContext = HookContextWithoutData.of("test-flag", FlagValueType.STRING, "default");

        HookData hookData = HookData.create();
        hookData.set("timing", System.currentTimeMillis());

        HookContext<String> contextWithHookData = HookContextWithData.of(originalContext, hookData);

        assertNull(originalContext.getHookData());
        assertNotNull(contextWithHookData.getHookData());
        assertNotNull(contextWithHookData.getHookData().get("timing"));
    }
}

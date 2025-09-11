package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void shouldCreateHookContextWithHookData() {
        HookData hookData = HookData.create();
        hookData.set("test", "value");

        HookContext<String> context = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(new ImmutableContext())
                .hookData(hookData)
                .build();

        assertNotNull(context.getHookData());
        assertEquals("value", context.getHookData().get("test"));
    }

    @Test
    void shouldCreateHookContextWithoutHookData() {
        HookContext<String> context = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(new ImmutableContext())
                .build();

        assertNull(context.getHookData());
    }

    @Test
    void shouldCreateHookContextWithHookDataUsingWith() {
        HookContext<String> originalContext = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(new ImmutableContext())
                .build();

        HookData hookData = HookData.create();
        hookData.set("timing", System.currentTimeMillis());

        HookContext<String> contextWithHookData = HookContextWithData.of(originalContext, hookData);

        assertNull(originalContext.getHookData());
        assertNotNull(contextWithHookData.getHookData());
        assertNotNull(contextWithHookData.getHookData().get("timing"));
    }
}

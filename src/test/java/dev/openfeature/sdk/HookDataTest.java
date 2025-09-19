package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HookDataTest {

    @Test
    void shouldStoreAndRetrieveValues() {
        HookData hookData = HookData.create();

        hookData.set("key1", "value1");
        hookData.set("key2", 42);
        hookData.set("key3", true);

        assertEquals("value1", hookData.get("key1"));
        assertEquals(42, hookData.get("key2"));
        assertEquals(true, hookData.get("key3"));
    }

    @Test
    void shouldReturnNullForMissingKeys() {
        HookData hookData = HookData.create();

        assertNull(hookData.get("nonexistent"));
    }

    @Test
    void shouldSupportTypeSafeRetrieval() {
        HookData hookData = HookData.create();

        hookData.set("string", "hello");
        hookData.set("integer", 123);
        hookData.set("boolean", false);

        assertEquals("hello", hookData.get("string", String.class));
        assertEquals(Integer.valueOf(123), hookData.get("integer", Integer.class));
        assertEquals(Boolean.FALSE, hookData.get("boolean", Boolean.class));
    }

    @Test
    void shouldReturnNullForMissingKeysWithType() {
        HookData hookData = HookData.create();

        assertNull(hookData.get("missing", String.class));
    }

    @Test
    void shouldThrowClassCastExceptionForWrongType() {
        HookData hookData = HookData.create();

        hookData.set("string", "not a number");

        assertThrows(ClassCastException.class, () -> {
            hookData.get("string", Integer.class);
        });
    }

    @Test
    void shouldOverwriteExistingValues() {
        HookData hookData = HookData.create();

        hookData.set("key", "original");
        assertEquals("original", hookData.get("key"));

        hookData.set("key", "updated");
        assertEquals("updated", hookData.get("key"));
    }

    @Test
    void shouldSupportNullValues() {
        HookData hookData = HookData.create();

        hookData.set("nullKey", null);
        assertNull(hookData.get("nullKey"));
        assertNull(hookData.get("nullKey", String.class));
    }
}

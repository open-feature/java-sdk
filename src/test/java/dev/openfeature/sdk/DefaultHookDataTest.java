package dev.openfeature.sdk;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultHookDataTest {

    @Test
    void shouldStoreAndRetrieveValues() {
        var hookData = new DefaultHookData();

        hookData.set("key1", "value1");
        hookData.set("key2", 42);
        hookData.set("key3", true);

        assertEquals("value1", hookData.get("key1"));
        assertEquals(42, hookData.get("key2"));
        assertEquals(true, hookData.get("key3"));
    }

    @Test
    void shouldReturnNullForMissingKeys() {
        var hookData = new DefaultHookData();

        assertNull(hookData.get("nonexistent"));
    }

    @Test
    void shouldSupportTypeSafeRetrieval() {
        var hookData = new DefaultHookData();

        hookData.set("string", "hello");
        hookData.set("integer", 123);
        hookData.set("boolean", false);

        assertEquals("hello", hookData.get("string", String.class));
        assertEquals(Integer.valueOf(123), hookData.get("integer", Integer.class));
        assertEquals(Boolean.FALSE, hookData.get("boolean", Boolean.class));
    }

    @Test
    void shouldReturnNullForMissingKeysWithType() {
        var hookData = new DefaultHookData();

        assertNull(hookData.get("missing", String.class));
    }

    @Test
    void shouldThrowClassCastExceptionForWrongType() {
        var hookData = new DefaultHookData();

        hookData.set("string", "not a number");

        assertThrows(ClassCastException.class, () -> {
            hookData.get("string", Integer.class);
        });
    }

    @Test
    void shouldOverwriteExistingValues() {
        var hookData = new DefaultHookData();

        hookData.set("key", "original");
        assertEquals("original", hookData.get("key"));

        hookData.set("key", "updated");
        assertEquals("updated", hookData.get("key"));
    }

    @Test
    void shouldSupportNullValues() {
        var hookData = new DefaultHookData();

        hookData.set("nullKey", null);
        assertNull(hookData.get("nullKey"));
        assertNull(hookData.get("nullKey", String.class));
    }
}

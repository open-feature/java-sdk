package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    void shouldBeThreadSafe() throws InterruptedException {
        HookData hookData = HookData.create();
        int threadCount = 10;
        int operationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread-" + threadId + "-key-" + j;
                        String value = "thread-" + threadId + "-value-" + j;
                        hookData.set(key, value);
                        assertEquals(value, hookData.get(key));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
    }

    @Test
    void shouldSupportNullValues() {
        HookData hookData = HookData.create();

        hookData.set("nullKey", null);
        assertNull(hookData.get("nullKey"));
        assertNull(hookData.get("nullKey", String.class));
    }
}

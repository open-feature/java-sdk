package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FlagEvaluationOptionsTest {

    // Simple mock hook for testing
    private static class TestHook implements Hook<String> {
        private final String name;

        TestHook(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "TestHook{" + name + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestHook)) return false;
            TestHook testHook = (TestHook) obj;
            return name.equals(testHook.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    @Test
    void defaultConstructor_shouldCreateEmptyOptions() {
        FlagEvaluationOptions options = new FlagEvaluationOptions();

        assertNotNull(options.getHooks());
        assertTrue(options.getHooks().isEmpty());
        assertNotNull(options.getHookHints());
        assertTrue(options.getHookHints().isEmpty());
    }

    @Test
    void constructor_shouldCreateOptionsWithValues() {
        List<Hook> hooks = Arrays.asList(new TestHook("hook1"), new TestHook("hook2"));
        Map<String, Object> hints = Map.of("key1", "value1", "key2", 42);

        FlagEvaluationOptions options = new FlagEvaluationOptions(hooks, hints);

        assertEquals(2, options.getHooks().size());
        assertEquals(hooks, options.getHooks());
        assertEquals(2, options.getHookHints().size());
        assertEquals("value1", options.getHookHints().get("key1"));
        assertEquals(42, options.getHookHints().get("key2"));
    }

    @Test
    void constructor_shouldHandleNullValues() {
        FlagEvaluationOptions options = new FlagEvaluationOptions(null, null);

        assertNotNull(options.getHooks());
        assertTrue(options.getHooks().isEmpty());
        assertNotNull(options.getHookHints());
        assertTrue(options.getHookHints().isEmpty());
    }

    @Test
    void getHooks_shouldReturnDefensiveCopy() {
        List<Hook> originalHooks = new ArrayList<>(Arrays.asList(new TestHook("hook1")));
        FlagEvaluationOptions options = new FlagEvaluationOptions(originalHooks, null);

        List<Hook> returnedHooks = options.getHooks();

        // Should not be the same instance
        assertNotSame(originalHooks, returnedHooks);
        assertNotSame(returnedHooks, options.getHooks()); // Each call returns new instance

        // Modifying returned list should not affect options
        returnedHooks.add(new TestHook("hook2"));
        assertEquals(1, options.getHooks().size());

        // Modifying original list should not affect options
        originalHooks.add(new TestHook("hook3"));
        assertEquals(1, options.getHooks().size());
    }

    @Test
    void getHookHints_shouldReturnDefensiveCopy() {
        Map<String, Object> originalHints = new HashMap<>();
        originalHints.put("key1", "value1");
        FlagEvaluationOptions options = new FlagEvaluationOptions(null, originalHints);

        Map<String, Object> returnedHints = options.getHookHints();

        // Should not be the same instance
        assertNotSame(originalHints, returnedHints);
        assertNotSame(returnedHints, options.getHookHints()); // Each call returns new instance

        // Modifying returned map should not affect options
        returnedHints.put("key2", "value2");
        assertEquals(1, options.getHookHints().size());

        // Modifying original map should not affect options
        originalHints.put("key3", "value3");
        assertEquals(1, options.getHookHints().size());
    }

    @Test
    void builder_shouldCreateEmptyOptions() {
        FlagEvaluationOptions options = FlagEvaluationOptions.builder().build();

        assertNotNull(options.getHooks());
        assertTrue(options.getHooks().isEmpty());
        assertNotNull(options.getHookHints());
        assertTrue(options.getHookHints().isEmpty());
    }

    @Test
    void builder_shouldAddSingleHook() {
        TestHook hook = new TestHook("test-hook");
        FlagEvaluationOptions options =
                FlagEvaluationOptions.builder().hook(hook).build();

        assertEquals(1, options.getHooks().size());
        assertEquals(hook, options.getHooks().get(0));
    }

    @Test
    void builder_shouldAddMultipleHooksIndividually() {
        TestHook hook1 = new TestHook("hook1");
        TestHook hook2 = new TestHook("hook2");

        FlagEvaluationOptions options =
                FlagEvaluationOptions.builder().hook(hook1).hook(hook2).build();

        assertEquals(2, options.getHooks().size());
        assertEquals(hook1, options.getHooks().get(0));
        assertEquals(hook2, options.getHooks().get(1));
    }

    @Test
    void builder_shouldSetHooksList() {
        List<Hook> hooks = Arrays.asList(new TestHook("hook1"), new TestHook("hook2"));

        FlagEvaluationOptions options =
                FlagEvaluationOptions.builder().hooks(hooks).build();

        assertEquals(2, options.getHooks().size());
        assertEquals(hooks, options.getHooks());
    }

    @Test
    void builder_shouldHandleNullHooksList() {
        FlagEvaluationOptions options =
                FlagEvaluationOptions.builder().hooks(null).build();

        assertNotNull(options.getHooks());
        assertTrue(options.getHooks().isEmpty());
    }

    @Test
    void builder_shouldSetHookHints() {
        Map<String, Object> hints = Map.of("key1", "value1", "key2", 42);

        FlagEvaluationOptions options =
                FlagEvaluationOptions.builder().hookHints(hints).build();

        assertEquals(2, options.getHookHints().size());
        assertEquals("value1", options.getHookHints().get("key1"));
        assertEquals(42, options.getHookHints().get("key2"));
    }

    @Test
    void builder_shouldHandleNullHookHints() {
        FlagEvaluationOptions options =
                FlagEvaluationOptions.builder().hookHints(null).build();

        assertNotNull(options.getHookHints());
        assertTrue(options.getHookHints().isEmpty());
    }

    @Test
    void builder_shouldCombineHooksAndHints() {
        TestHook hook1 = new TestHook("hook1");
        TestHook hook2 = new TestHook("hook2");
        Map<String, Object> hints = Map.of("key", "value");

        FlagEvaluationOptions options = FlagEvaluationOptions.builder()
                .hook(hook1)
                .hook(hook2)
                .hookHints(hints)
                .build();

        assertEquals(2, options.getHooks().size());
        assertEquals(1, options.getHookHints().size());
        assertEquals("value", options.getHookHints().get("key"));
    }

    @Test
    void builder_shouldOverrideHooksListWhenSetAfterIndividualHooks() {
        TestHook individualHook = new TestHook("individual");
        List<Hook> hooksList = Arrays.asList(new TestHook("list1"), new TestHook("list2"));

        FlagEvaluationOptions options = FlagEvaluationOptions.builder()
                .hook(individualHook)
                .hooks(hooksList) // This should replace the individual hook
                .build();

        assertEquals(2, options.getHooks().size());
        assertEquals(hooksList, options.getHooks());
    }

    @Test
    void builder_shouldAddToExistingHooksAfterList() {
        List<Hook> hooksList = Arrays.asList(new TestHook("list1"));
        TestHook additionalHook = new TestHook("additional");

        FlagEvaluationOptions options = FlagEvaluationOptions.builder()
                .hooks(hooksList)
                .hook(additionalHook) // This should add to the list
                .build();

        assertEquals(2, options.getHooks().size());
        assertEquals("list1", ((TestHook) options.getHooks().get(0)).name);
        assertEquals("additional", ((TestHook) options.getHooks().get(1)).name);
    }

    @Test
    void equals_shouldWorkCorrectly() {
        TestHook hook = new TestHook("test");
        Map<String, Object> hints = Map.of("key", "value");

        FlagEvaluationOptions options1 =
                FlagEvaluationOptions.builder().hook(hook).hookHints(hints).build();

        FlagEvaluationOptions options2 =
                FlagEvaluationOptions.builder().hook(hook).hookHints(hints).build();

        FlagEvaluationOptions options3 = FlagEvaluationOptions.builder()
                .hook(new TestHook("different"))
                .hookHints(hints)
                .build();

        // Same content should be equal
        assertEquals(options1, options2);
        assertEquals(options2, options1);

        // Different hooks should not be equal
        assertNotEquals(options1, options3);

        // Self-equality
        assertEquals(options1, options1);

        // Null comparison
        assertNotEquals(options1, null);

        // Different class comparison
        assertNotEquals(options1, "not options");
    }

    @Test
    void hashCode_shouldBeConsistent() {
        TestHook hook = new TestHook("test");
        Map<String, Object> hints = Map.of("key", "value");

        FlagEvaluationOptions options1 =
                FlagEvaluationOptions.builder().hook(hook).hookHints(hints).build();

        FlagEvaluationOptions options2 =
                FlagEvaluationOptions.builder().hook(hook).hookHints(hints).build();

        assertEquals(options1.hashCode(), options2.hashCode());
    }

    @Test
    void toString_shouldIncludeHooksAndHints() {
        TestHook hook = new TestHook("test");
        Map<String, Object> hints = Map.of("key", "value");

        FlagEvaluationOptions options =
                FlagEvaluationOptions.builder().hook(hook).hookHints(hints).build();

        String toString = options.toString();
        assertTrue(toString.contains("FlagEvaluationOptions"));
        assertTrue(toString.contains("hooks"));
        assertTrue(toString.contains("hookHints"));
    }
}

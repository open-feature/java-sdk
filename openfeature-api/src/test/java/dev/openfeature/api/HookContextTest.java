package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HookContextTest {

    // Simple mock implementations for testing
    private static class TestClientMetadata implements ClientMetadata {
        private final String domain;

        TestClientMetadata(String domain) {
            this.domain = domain;
        }

        @Override
        public String getDomain() {
            return domain;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestClientMetadata)) return false;
            TestClientMetadata that = (TestClientMetadata) obj;
            return domain.equals(that.domain);
        }

        @Override
        public int hashCode() {
            return domain.hashCode();
        }
    }

    private static class TestProviderMetadata implements ProviderMetadata {
        private final String name;

        TestProviderMetadata(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestProviderMetadata)) return false;
            TestProviderMetadata that = (TestProviderMetadata) obj;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    @Test
    void builder_shouldCreateHookContextWithRequiredFields() {
        String flagKey = "test-flag";
        String defaultValue = "default";
        EvaluationContext context = new ImmutableContext();

        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey(flagKey)
                .type(FlagValueType.STRING)
                .defaultValue(defaultValue)
                .ctx(context)
                .build();

        assertEquals(flagKey, hookContext.getFlagKey());
        assertEquals(FlagValueType.STRING, hookContext.getType());
        assertEquals(defaultValue, hookContext.getDefaultValue());
        assertSame(context, hookContext.getCtx());
        assertNull(hookContext.getClientMetadata());
        assertNull(hookContext.getProviderMetadata());
    }

    @Test
    void builder_shouldCreateHookContextWithAllFields() {
        String flagKey = "test-flag";
        Integer defaultValue = 42;
        EvaluationContext context = new ImmutableContext();
        TestClientMetadata clientMetadata = new TestClientMetadata("test-client");
        TestProviderMetadata providerMetadata = new TestProviderMetadata("test-provider");

        HookContext<Integer> hookContext = HookContext.<Integer>builder()
                .flagKey(flagKey)
                .type(FlagValueType.INTEGER)
                .defaultValue(defaultValue)
                .ctx(context)
                .clientMetadata(clientMetadata)
                .providerMetadata(providerMetadata)
                .build();

        assertEquals(flagKey, hookContext.getFlagKey());
        assertEquals(FlagValueType.INTEGER, hookContext.getType());
        assertEquals(defaultValue, hookContext.getDefaultValue());
        assertSame(context, hookContext.getCtx());
        assertSame(clientMetadata, hookContext.getClientMetadata());
        assertSame(providerMetadata, hookContext.getProviderMetadata());
    }

    @Test
    void builder_shouldThrowWhenFlagKeyIsNull() {
        assertThrows(NullPointerException.class, () -> {
            HookContext.<String>builder()
                    .flagKey(null)
                    .type(FlagValueType.STRING)
                    .defaultValue("default")
                    .ctx(new ImmutableContext())
                    .build();
        });
    }

    @Test
    void builder_shouldThrowWhenTypeIsNull() {
        assertThrows(NullPointerException.class, () -> {
            HookContext.<String>builder()
                    .flagKey("test-flag")
                    .type(null)
                    .defaultValue("default")
                    .ctx(new ImmutableContext())
                    .build();
        });
    }

    @Test
    void builder_shouldThrowWhenDefaultValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            HookContext.<String>builder()
                    .flagKey("test-flag")
                    .type(FlagValueType.STRING)
                    .defaultValue(null)
                    .ctx(new ImmutableContext())
                    .build();
        });
    }

    @Test
    void builder_shouldThrowWhenCtxIsNull() {
        assertThrows(NullPointerException.class, () -> {
            HookContext.<String>builder()
                    .flagKey("test-flag")
                    .type(FlagValueType.STRING)
                    .defaultValue("default")
                    .ctx(null)
                    .build();
        });
    }

    @Test
    void builder_shouldAllowNullOptionalFields() {
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(new ImmutableContext())
                .clientMetadata(null)
                .providerMetadata(null)
                .build();

        assertEquals("test-flag", hookContext.getFlagKey());
        assertNull(hookContext.getClientMetadata());
        assertNull(hookContext.getProviderMetadata());
    }

    @Test
    void builder_shouldSupportDifferentTypes() {
        // Test with Boolean
        HookContext<Boolean> boolContext = HookContext.<Boolean>builder()
                .flagKey("bool-flag")
                .type(FlagValueType.BOOLEAN)
                .defaultValue(true)
                .ctx(new ImmutableContext())
                .build();

        assertEquals(FlagValueType.BOOLEAN, boolContext.getType());
        assertEquals(true, boolContext.getDefaultValue());

        // Test with Double
        HookContext<Double> doubleContext = HookContext.<Double>builder()
                .flagKey("double-flag")
                .type(FlagValueType.DOUBLE)
                .defaultValue(3.14)
                .ctx(new ImmutableContext())
                .build();

        assertEquals(FlagValueType.DOUBLE, doubleContext.getType());
        assertEquals(3.14, doubleContext.getDefaultValue());
    }

    @Test
    void equals_shouldWorkCorrectly() {
        EvaluationContext context = new ImmutableContext();
        TestClientMetadata clientMetadata = new TestClientMetadata("client");
        TestProviderMetadata providerMetadata = new TestProviderMetadata("provider");

        HookContext<String> context1 = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(context)
                .clientMetadata(clientMetadata)
                .providerMetadata(providerMetadata)
                .build();

        HookContext<String> context2 = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(context)
                .clientMetadata(clientMetadata)
                .providerMetadata(providerMetadata)
                .build();

        HookContext<String> context3 = HookContext.<String>builder()
                .flagKey("different-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(context)
                .build();

        // Same content should be equal
        assertEquals(context1, context2);
        assertEquals(context2, context1);

        // Different flag key should not be equal
        assertNotEquals(context1, context3);

        // Self-equality
        assertEquals(context1, context1);

        // Null comparison
        assertNotEquals(context1, null);

        // Different class comparison
        assertNotEquals(context1, "not a context");
    }

    @Test
    void equals_shouldHandleDifferentGenericTypes() {
        EvaluationContext context = new ImmutableContext();

        HookContext<String> stringContext = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(context)
                .build();

        HookContext<Integer> intContext = HookContext.<Integer>builder()
                .flagKey("test-flag")
                .type(FlagValueType.INTEGER)
                .defaultValue(42)
                .ctx(context)
                .build();

        // Different types should not be equal
        assertNotEquals(stringContext, intContext);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        EvaluationContext context = new ImmutableContext();

        HookContext<String> context1 = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(context)
                .build();

        HookContext<String> context2 = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(context)
                .build();

        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    void toString_shouldIncludeAllFields() {
        TestClientMetadata clientMetadata = new TestClientMetadata("client");
        TestProviderMetadata providerMetadata = new TestProviderMetadata("provider");

        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(new ImmutableContext())
                .clientMetadata(clientMetadata)
                .providerMetadata(providerMetadata)
                .build();

        String toString = hookContext.toString();
        assertTrue(toString.contains("HookContext"));
        assertTrue(toString.contains("test-flag"));
        assertTrue(toString.contains("STRING"));
        assertTrue(toString.contains("default"));
        assertTrue(toString.contains("client"));
        assertTrue(toString.contains("provider"));
    }

    @Test
    void immutability_shouldPreventModificationViaBuilder() {
        HookContext.Builder<String> builder = HookContext.<String>builder()
                .flagKey("test-flag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(new ImmutableContext());

        HookContext<String> hookContext = builder.build();

        // Modifying builder after build should not affect built context
        TestClientMetadata newMetadata = new TestClientMetadata("new-client");
        builder.clientMetadata(newMetadata);

        assertNull(hookContext.getClientMetadata());
    }

    @Test
    void genericTypeSupport_shouldWorkCorrectly() {
        // Test that we can have different generic types
        HookContext<Value> valueContext = HookContext.<Value>builder()
                .flagKey("value-flag")
                .type(FlagValueType.OBJECT)
                .defaultValue(new Value("test"))
                .ctx(new ImmutableContext())
                .build();

        assertEquals(FlagValueType.OBJECT, valueContext.getType());
        assertEquals("test", valueContext.getDefaultValue().asString());
    }
}

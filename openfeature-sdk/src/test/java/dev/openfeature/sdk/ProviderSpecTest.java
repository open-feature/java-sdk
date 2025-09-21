package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.Provider;
import dev.openfeature.api.Reason;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.types.Metadata;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProviderSpecTest {

    private TestableNoOpProvider provider;
    private ErrorGeneratingProvider errorProvider;

    @BeforeEach
    void setUp() {
        provider = new TestableNoOpProvider();
        errorProvider = new ErrorGeneratingProvider();
    }

    @Specification(
            number = "2.1.1",
            text =
                    "The provider interface MUST define a metadata member or accessor, containing a name field or accessor of type string, which identifies the provider implementation.")
    @Test
    void name_accessor() {
        assertThat(provider.getMetadata())
                .isNotNull()
                .extracting(ProviderMetadata::getName)
                .isNotNull()
                .isInstanceOf(String.class);
    }

    @Specification(
            number = "2.2.1",
            text =
                    "The `feature provider` interface MUST define methods to resolve flag values, with parameters `flag key` (string, required), `default value` (boolean | number | string | structure, required) and `evaluation context` (optional), which returns a `resolution details` structure.")
    @Specification(
            number = "2.2.2.1",
            text =
                    "The feature provider interface MUST define methods for typed flag resolution, including boolean, numeric, string, and structure.")
    @Specification(
            number = "2.2.3",
            text =
                    "In cases of normal execution, the `provider` MUST populate the `resolution details` structure's `value` field with the resolved flag value.")
    @Specification(
            number = "2.2.8.1",
            text =
                    "The `resolution details` structure SHOULD accept a generic argument (or use an equivalent language feature) which indicates the type of the wrapped `value` field.")
    @Test
    void flag_value_set() {
        assertThat(provider.getIntegerEvaluation("key", 4, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getValue)
                .isNotNull()
                .isEqualTo(4);

        assertThat(provider.getDoubleEvaluation("key", 0.4, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getValue)
                .isNotNull()
                .isEqualTo(0.4);

        assertThat(provider.getStringEvaluation("key", "works", EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getValue)
                .isNotNull()
                .isEqualTo("works");

        assertThat(provider.getBooleanEvaluation("key", false, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getValue)
                .isNotNull()
                .isEqualTo(false);

        assertThat(provider.getObjectEvaluation("key", new Value(), EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getValue)
                .isNotNull();
    }

    @Specification(
            number = "2.2.5",
            text =
                    "The `provider` SHOULD populate the `resolution details` structure's `reason` field with `\"STATIC\"`, `\"DEFAULT\",` `\"TARGETING_MATCH\"`, `\"SPLIT\"`, `\"CACHED\"`, `\"DISABLED\"`, `\"UNKNOWN\"`, `\"STALE\"`, `\"ERROR\"` or some other string indicating the semantic reason for the returned flag value.")
    @Test
    void has_reason() {
        assertThat(provider.getBooleanEvaluation("key", false, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getReason)
                .isNotNull()
                .isEqualTo(Reason.DEFAULT.toString());
    }

    @Specification(
            number = "2.2.6",
            text =
                    "In cases of normal execution, the `provider` MUST NOT populate the `resolution details` structure's `error code` field, or otherwise must populate it with a null or falsy value.")
    @Test
    void no_error_code_by_default() {
        assertThat(provider.getBooleanEvaluation("key", false, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getErrorCode)
                .isNull();
    }

    @Specification(
            number = "2.2.7",
            text =
                    "In cases of abnormal execution, the `provider` MUST indicate an error using the idioms of the implementation language, with an associated `error code` and optional associated `error message`.")
    @Specification(
            number = "2.3.2",
            text =
                    "In cases of normal execution, the `provider` MUST NOT populate the `resolution details` structure's `error message` field, or otherwise must populate it with a null or falsy value.")
    @Specification(
            number = "2.3.3",
            text =
                    "In cases of abnormal execution, the `resolution details` structure's `error message` field MAY contain a string containing additional detail about the nature of the error.")
    @Test
    void error_handling_in_abnormal_execution() {
        // Test normal execution - no error code or message
        assertThat(provider.getBooleanEvaluation("normal-key", false, EvaluationContext.EMPTY))
                .satisfies(result -> {
                    assertThat(result.getErrorCode()).isNull();
                    assertThat(result.getErrorMessage()).isNull();
                });

        // Test abnormal execution - should have error code, may have error message
        assertThat(errorProvider.getBooleanEvaluation("error-key", false, EvaluationContext.EMPTY))
                .satisfies(result -> {
                    assertThat(result.getErrorCode()).isNotNull();
                    // Error message is optional but if present should be meaningful
                    if (result.getErrorMessage() != null) {
                        assertThat(result.getErrorMessage()).isNotEmpty();
                    }
                });
    }

    @Specification(
            number = "2.2.4",
            text =
                    "In cases of normal execution, the `provider` SHOULD populate the `resolution details` structure's `variant` field with a string identifier corresponding to the returned flag value.")
    @Test
    void variant_set() {
        assertThat(provider.getIntegerEvaluation("key", 4, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getVariant)
                .isNotNull();

        assertThat(provider.getDoubleEvaluation("key", 0.4, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getVariant)
                .isNotNull();

        assertThat(provider.getStringEvaluation("key", "works", EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getVariant)
                .isNotNull();

        assertThat(provider.getBooleanEvaluation("key", false, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getVariant)
                .isNotNull();
    }

    @Specification(
            number = "2.2.10",
            text =
                    "`flag metadata` MUST be a structure supporting the definition of arbitrary properties, with keys of type `string`, and values of type `boolean | string | number`.")
    @Test
    void flag_metadata_structure() {
        var metadata = Metadata.immutableBuilder()
                .add("bool", true)
                .add("double", 1.1d)
                .add("float", 2.2f)
                .add("int", 3)
                .add("long", 1L)
                .add("string", "str")
                .build();

        assertThat(metadata).satisfies(m -> {
            assertThat(m.getBoolean("bool")).isTrue();
            assertThat(m.getDouble("double")).isEqualTo(1.1d);
            assertThat(m.getFloat("float")).isEqualTo(2.2f);
            assertThat(m.getInteger("int")).isEqualTo(3);
            assertThat(m.getLong("long")).isEqualTo(1L);
            assertThat(m.getString("string")).isEqualTo("str");
        });
    }

    @Specification(
            number = "2.3.1",
            text =
                    "The provider interface MUST define a provider hook mechanism which can be optionally implemented in order to add hook instances to the evaluation life-cycle.")
    @Test
    void provider_hooks() {
        assertThat(provider.getHooks()).isNotNull().isEmpty();
    }

    @Specification(
            number = "2.2.9",
            text = "The provider SHOULD populate the resolution details structure's flag metadata field.")
    @Test
    void provider_populates_flag_metadata() {
        assertThat(provider.getBooleanEvaluation("key", false, EvaluationContext.EMPTY))
                .extracting(ProviderEvaluation::getFlagMetadata)
                .satisfies(flagMetadata -> {
                    // Flag metadata may or may not be present, but if present should be valid
                    if (flagMetadata != null) {
                        assertThat(flagMetadata).isInstanceOf(Metadata.class);
                    }
                });
    }

    @Specification(
            number = "2.4.1",
            text =
                    "The provider MAY define an initialization function which accepts the global evaluation context as an argument and performs initialization logic relevant to the provider.")
    @Specification(
            number = "2.4.2.1",
            text =
                    "If the provider's initialize function fails to render the provider ready to evaluate flags, it SHOULD abnormally terminate.")
    @Test
    void provider_initialization() {
        TestableNoOpProvider testProvider = new TestableNoOpProvider();

        // Test normal initialization - should not throw
        testProvider.initialize(EvaluationContext.EMPTY);
        assertThat(testProvider.isInitialized()).isTrue();

        // Test abnormal initialization - should throw exception
        TestableNoOpProvider errorInitProvider = new TestableNoOpProvider();
        errorInitProvider.setFailOnInit(true);

        assertThatThrownBy(() -> errorInitProvider.initialize(EvaluationContext.EMPTY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Initialization failed");
    }

    @Specification(
            number = "2.5.1",
            text = "The provider MAY define a mechanism to gracefully shutdown and dispose of resources.")
    @Specification(
            number = "2.5.2",
            text =
                    "After a provider's shutdown function has terminated, the provider SHOULD revert to its uninitialized state.")
    @Test
    void provider_shutdown() {
        TestableNoOpProvider testProvider = new TestableNoOpProvider();
        testProvider.initialize(EvaluationContext.EMPTY);

        assertThat(testProvider.isInitialized()).isTrue();

        // Test shutdown
        testProvider.shutdown();

        assertThat(testProvider).satisfies(provider -> {
            assertThat(provider.isShutdown()).isTrue();
            assertThat(provider.isInitialized()).isFalse(); // Should revert to uninitialized
        });
    }

    @Specification(
            number = "2.6.1",
            text =
                    "The provider MAY define an on context changed function, which takes an argument for the previous context and the newly set context, in order to respond to an evaluation context change.")
    @Test
    void context_change_handler() {
        TestableNoOpProvider testProvider = new TestableNoOpProvider();

        EvaluationContext oldContext = EvaluationContext.EMPTY;
        EvaluationContext newContext = EvaluationContext.immutableOf("new-targeting-key", null);

        // Test context change (if provider supports it)
        testProvider.onContextSet(oldContext, newContext);

        // Verify the provider handled the context change appropriately
        assertThat(testProvider.hasContextChangeBeenCalled()).isTrue();
    }

    // Helper classes for testing

    /**
     * Testable version of provider that allows controlling behavior for testing
     */
    private static class TestableNoOpProvider implements Provider {
        private boolean failOnInit = false;
        private boolean isShutdown = false;
        private boolean isInitialized = false;
        private boolean contextChangeCalled = false;

        @Override
        public ProviderMetadata getMetadata() {
            return () -> "Test No-Op Provider";
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, defaultValue.toString(), Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(
                String key, String defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, defaultValue, Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, defaultValue.toString(), Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(
                String key, Double defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, defaultValue.toString(), Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(
                String key, Value defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, defaultValue.toString(), Reason.DEFAULT.toString(), null);
        }

        public void initialize(EvaluationContext evaluationContext) {
            if (failOnInit) {
                throw new RuntimeException("Initialization failed as requested");
            }
            isInitialized = true;
            isShutdown = false;
        }

        public void shutdown() {
            isShutdown = true;
            isInitialized = false; // Revert to uninitialized state per spec 2.5.2
        }

        public void onContextSet(EvaluationContext oldContext, EvaluationContext newContext) {
            contextChangeCalled = true;
        }

        public void setFailOnInit(boolean failOnInit) {
            this.failOnInit = failOnInit;
        }

        public boolean isShutdown() {
            return isShutdown;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        public boolean hasContextChangeBeenCalled() {
            return contextChangeCalled;
        }
    }

    /**
     * Provider that generates errors for testing error handling
     */
    private static class ErrorGeneratingProvider implements Provider {
        @Override
        public ProviderMetadata getMetadata() {
            return () -> "Error Generating Provider";
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext evaluationContext) {
            if ("error-key".equals(key)) {
                return ProviderEvaluation.of(
                        defaultValue,
                        null,
                        Reason.ERROR.toString(),
                        ErrorCode.GENERAL,
                        "simulated error for testing",
                        null);
            }
            return ProviderEvaluation.of(defaultValue, null, Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(
                String key, String defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(
                String key, Double defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, Reason.DEFAULT.toString(), null);
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(
                String key, Value defaultValue, EvaluationContext evaluationContext) {
            return ProviderEvaluation.of(defaultValue, null, Reason.DEFAULT.toString(), null);
        }
    }
}

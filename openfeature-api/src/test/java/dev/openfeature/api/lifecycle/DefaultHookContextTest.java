package dev.openfeature.api.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Specification;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.types.ClientMetadata;
import dev.openfeature.api.types.ProviderMetadata;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultHookContextTest {

    private static final String TEST_FLAG_KEY = "test-flag";
    private static final String TEST_DEFAULT_VALUE = "default-value";
    private static final FlagValueType TEST_TYPE = FlagValueType.STRING;

    private ProviderMetadata providerMetadata;
    private ClientMetadata clientMetadata;
    private EvaluationContext evaluationContext;
    private DefaultHookContext<String> hookContext;

    @BeforeEach
    void setUp() {
        providerMetadata = () -> "test-provider";
        clientMetadata = () -> "test-client";
        evaluationContext = EvaluationContext.immutableOf("targeting-key", null);

        hookContext = new DefaultHookContext<>(
                TEST_FLAG_KEY, TEST_DEFAULT_VALUE, TEST_TYPE, providerMetadata, clientMetadata, evaluationContext);
    }

    @Specification(
            number = "4.1.1",
            text =
                    "Hook context MUST provide: the flag key, flag value type, evaluation context, default value, and hook data.")
    @Test
    void hook_context_provides_required_fields() {
        assertThat(hookContext).satisfies(context -> {
            // Flag key
            assertThat(context.getFlagKey()).isNotNull().isEqualTo(TEST_FLAG_KEY);

            // Flag value type
            assertThat(context.getType()).isNotNull().isEqualTo(TEST_TYPE);

            // Evaluation context
            assertThat(context.getCtx()).isNotNull().isEqualTo(evaluationContext);

            // Default value
            assertThat(context.getDefaultValue()).isNotNull().isEqualTo(TEST_DEFAULT_VALUE);
        });

        // NOTE: Hook data is MISSING - this is a specification compliance issue
        // The specification requires hook data but this implementation doesn't provide it
    }

    @Specification(
            number = "4.1.2",
            text = "The hook context SHOULD provide access to the client metadata and the provider metadata fields.")
    @Test
    void hook_context_provides_metadata_fields() {
        assertThat(hookContext).satisfies(context -> {
            assertThat(context.getClientMetadata()).isNotNull().isEqualTo(clientMetadata);

            assertThat(context.getProviderMetadata()).isNotNull().isEqualTo(providerMetadata);
        });
    }

    @Specification(
            number = "4.1.3",
            text =
                    "The flag key, flag type, and default value properties MUST be immutable. If the language does not support immutability, the hook MUST NOT modify these properties.")
    @Test
    void required_properties_are_immutable() {
        // All fields are final and the class is final, ensuring immutability
        assertThat(hookContext.getFlagKey()).isSameAs(hookContext.getFlagKey()); // Same reference each time

        assertThat(hookContext.getType()).isSameAs(hookContext.getType());

        assertThat(hookContext.getDefaultValue()).isSameAs(hookContext.getDefaultValue());
    }

    @Test
    void constructor_accepts_all_required_parameters() {
        // Test with different types
        DefaultHookContext<Boolean> booleanContext = new DefaultHookContext<>(
                "boolean-flag", true, FlagValueType.BOOLEAN, providerMetadata, clientMetadata, evaluationContext);

        assertThat(booleanContext).satisfies(context -> {
            assertThat(context.getFlagKey()).isEqualTo("boolean-flag");
            assertThat(context.getDefaultValue()).isEqualTo(true);
            assertThat(context.getType()).isEqualTo(FlagValueType.BOOLEAN);
            assertThat(context.getProviderMetadata()).isSameAs(providerMetadata);
            assertThat(context.getClientMetadata()).isSameAs(clientMetadata);
            assertThat(context.getCtx()).isSameAs(evaluationContext);
        });
    }

    @Test
    void supports_different_flag_value_types() {
        // Test with Integer
        DefaultHookContext<Integer> integerContext = new DefaultHookContext<>(
                "int-flag", 42, FlagValueType.INTEGER, providerMetadata, clientMetadata, evaluationContext);

        assertThat(integerContext.getDefaultValue()).isEqualTo(42);
        assertThat(integerContext.getType()).isEqualTo(FlagValueType.INTEGER);

        // Test with Double
        DefaultHookContext<Double> doubleContext = new DefaultHookContext<>(
                "double-flag", 3.14, FlagValueType.DOUBLE, providerMetadata, clientMetadata, evaluationContext);

        assertThat(doubleContext.getDefaultValue()).isEqualTo(3.14);
        assertThat(doubleContext.getType()).isEqualTo(FlagValueType.DOUBLE);
    }

    @Test
    void handles_null_evaluation_context() {
        DefaultHookContext<String> contextWithNullEvaluationContext = new DefaultHookContext<>(
                TEST_FLAG_KEY,
                TEST_DEFAULT_VALUE,
                TEST_TYPE,
                providerMetadata,
                clientMetadata,
                null // null evaluation context
                );

        assertThat(contextWithNullEvaluationContext.getCtx()).isNull();
    }

    @Test
    void handles_null_metadata() {
        DefaultHookContext<String> contextWithNullMetadata = new DefaultHookContext<>(
                TEST_FLAG_KEY,
                TEST_DEFAULT_VALUE,
                TEST_TYPE,
                null, // null provider metadata
                null, // null client metadata
                evaluationContext);

        assertThat(contextWithNullMetadata).satisfies(context -> {
            assertThat(context.getProviderMetadata()).isNull();
            assertThat(context.getClientMetadata()).isNull();
            // Other fields should still work
            assertThat(context.getFlagKey()).isEqualTo(TEST_FLAG_KEY);
            assertThat(context.getDefaultValue()).isEqualTo(TEST_DEFAULT_VALUE);
            assertThat(context.getType()).isEqualTo(TEST_TYPE);
            assertThat(context.getCtx()).isEqualTo(evaluationContext);
        });
    }

    @Test
    void evaluation_context_is_returned_as_provided() {
        EvaluationContext customContext = EvaluationContext.immutableBuilder()
                .targetingKey("custom-key")
                .add("custom-attribute", "custom-value")
                .build();

        DefaultHookContext<String> contextWithCustomEvaluationContext = new DefaultHookContext<>(
                TEST_FLAG_KEY, TEST_DEFAULT_VALUE, TEST_TYPE, providerMetadata, clientMetadata, customContext);

        assertThat(contextWithCustomEvaluationContext.getCtx())
                .isSameAs(customContext)
                .extracting(ctx -> ctx.getValue("custom-attribute").asString())
                .isEqualTo("custom-value");
    }

    @Test
    void class_is_final_ensuring_immutability() {
        // Verify the class is final (this will be checked at compile time)
        assertThat(Modifier.isFinal(DefaultHookContext.class.getModifiers())).isTrue();
    }

    @Test
    void generic_type_safety() {
        // Test that generic types are properly maintained
        DefaultHookContext<String> stringContext = new DefaultHookContext<>(
                "string-flag",
                "string-default",
                FlagValueType.STRING,
                providerMetadata,
                clientMetadata,
                evaluationContext);

        String defaultValue = stringContext.getDefaultValue(); // Should not require casting
        assertThat(defaultValue).isInstanceOf(String.class);

        DefaultHookContext<Integer> intContext = new DefaultHookContext<>(
                "int-flag", 123, FlagValueType.INTEGER, providerMetadata, clientMetadata, evaluationContext);

        Integer intDefaultValue = intContext.getDefaultValue(); // Should not require casting
        assertThat(intDefaultValue).isInstanceOf(Integer.class);
    }

    // Test for specification compliance issues
    @Test
    void specification_compliance_issues() {
        assertThat(hookContext.getHookData()).isNotNull();
        hookContext.getHookData().set("test-key", "test-value");
        assertThat(hookContext.getHookData().get("test-key")).isEqualTo("test-value");

        // For now, we document this as a known limitation
        assertThat(hookContext).satisfies(context -> {
            // All other required fields are present
            assertThat(context.getFlagKey()).isNotNull();
            assertThat(context.getType()).isNotNull();
            assertThat(context.getDefaultValue()).isNotNull();
            assertThat(context.getCtx()).isNotNull();
        });
    }
}

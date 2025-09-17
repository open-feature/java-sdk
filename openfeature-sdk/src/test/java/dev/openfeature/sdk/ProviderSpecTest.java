package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.*;
import dev.openfeature.api.internal.noop.NoOpProvider;
import org.junit.jupiter.api.Test;

public class ProviderSpecTest {
    NoOpProvider p = new NoOpProvider();

    @Specification(
            number = "2.1.1",
            text =
                    "The provider interface MUST define a metadata member or accessor, containing a name field or accessor of type string, which identifies the provider implementation.")
    @Test
    void name_accessor() {
        assertNotNull(p.getName());
    }

    @Specification(
            number = "2.2.2.1",
            text = "The feature provider interface MUST define methods for typed "
                    + "flag resolution, including boolean, numeric, string, and structure.")
    @Specification(
            number = "2.2.3",
            text =
                    "In cases of normal execution, the `provider` MUST populate the `resolution details` structure's `value` field with the resolved flag value.")
    @Specification(
            number = "2.2.1",
            text =
                    "The `feature provider` interface MUST define methods to resolve flag values, with parameters `flag key` (string, required), `default value` (boolean | number | string | structure, required) and `evaluation context` (optional), which returns a `resolution details` structure.")
    @Specification(
            number = "2.2.8.1",
            text =
                    "The `resolution details` structure SHOULD accept a generic argument (or use an equivalent language feature) which indicates the type of the wrapped `value` field.")
    @Test
    void flag_value_set() {
        ProviderEvaluation<Integer> int_result = p.getIntegerEvaluation("key", 4, EvaluationContext.EMPTY);
        assertNotNull(int_result.getValue());

        ProviderEvaluation<Double> double_result = p.getDoubleEvaluation("key", 0.4, EvaluationContext.EMPTY);
        assertNotNull(double_result.getValue());

        ProviderEvaluation<String> string_result = p.getStringEvaluation("key", "works", EvaluationContext.EMPTY);
        assertNotNull(string_result.getValue());

        ProviderEvaluation<Boolean> boolean_result = p.getBooleanEvaluation("key", false, EvaluationContext.EMPTY);
        assertNotNull(boolean_result.getValue());

        ProviderEvaluation<Value> object_result = p.getObjectEvaluation("key", new Value(), EvaluationContext.EMPTY);
        assertNotNull(object_result.getValue());
    }

    @Specification(
            number = "2.2.5",
            text =
                    "The `provider` SHOULD populate the `resolution details` structure's `reason` field with `\"STATIC\"`, `\"DEFAULT\",` `\"TARGETING_MATCH\"`, `\"SPLIT\"`, `\"CACHED\"`, `\"DISABLED\"`, `\"UNKNOWN\"`, `\"STALE\"`, `\"ERROR\"` or some other string indicating the semantic reason for the returned flag value.")
    @Test
    void has_reason() {
        ProviderEvaluation<Boolean> result = p.getBooleanEvaluation("key", false, EvaluationContext.EMPTY);
        assertEquals(Reason.DEFAULT.toString(), result.getReason());
    }

    @Specification(
            number = "2.2.6",
            text =
                    "In cases of normal execution, the `provider` MUST NOT populate the `resolution details` structure's `error code` field, or otherwise must populate it with a null or falsy value.")
    @Test
    void no_error_code_by_default() {
        ProviderEvaluation<Boolean> result = p.getBooleanEvaluation("key", false, EvaluationContext.EMPTY);
        assertNull(result.getErrorCode());
    }

    @Specification(
            number = "2.2.7",
            text =
                    "In cases of abnormal execution, the `provider` **MUST** indicate an error using the idioms of the implementation language, with an associated `error code` and optional associated `error message`.")
    @Specification(
            number = "2.3.2",
            text =
                    "In cases of normal execution, the `provider` MUST NOT populate the `resolution details` structure's `error message` field, or otherwise must populate it with a null or falsy value.")
    @Specification(
            number = "2.3.3",
            text =
                    "In cases of abnormal execution, the `resolution details` structure's `error message` field MAY contain a string containing additional detail about the nature of the error.")
    @Test
    void up_to_provider_implementation() {}

    @Specification(
            number = "2.2.4",
            text =
                    "In cases of normal execution, the `provider` SHOULD populate the `resolution details` structure's `variant` field with a string identifier corresponding to the returned flag value.")
    @Test
    void variant_set() {
        ProviderEvaluation<Integer> int_result = p.getIntegerEvaluation("key", 4, EvaluationContext.EMPTY);
        assertNotNull(int_result.getReason());

        ProviderEvaluation<Double> double_result = p.getDoubleEvaluation("key", 0.4, EvaluationContext.EMPTY);
        assertNotNull(double_result.getReason());

        ProviderEvaluation<String> string_result = p.getStringEvaluation("key", "works", EvaluationContext.EMPTY);
        assertNotNull(string_result.getReason());

        ProviderEvaluation<Boolean> boolean_result = p.getBooleanEvaluation("key", false, EvaluationContext.EMPTY);
        assertNotNull(boolean_result.getReason());
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
                .add("long", 1l)
                .add("string", "str")
                .build();

        assertEquals(true, metadata.getBoolean("bool"));
        assertEquals(1.1d, metadata.getDouble("double"));
        assertEquals(2.2f, metadata.getFloat("float"));
        assertEquals(3, metadata.getInteger("int"));
        assertEquals(1l, metadata.getLong("long"));
        assertEquals("str", metadata.getString("string"));
    }

    @Specification(
            number = "2.3.1",
            text =
                    "The provider interface MUST define a provider hook mechanism which can be optionally implemented in order to add hook instances to the evaluation life-cycle.")
    @Specification(
            number = "4.4.1",
            text = "The API, Client, Provider, and invocation MUST have a method for registering hooks.")
    @Test
    void provider_hooks() {
        assertEquals(0, p.getProviderHooks().size());
    }

    @Specification(
            number = "2.4.2",
            text =
                    "The provider MAY define a status field/accessor which indicates the readiness of the provider, with possible values NOT_READY, READY, or ERROR.")
    @Test
    void defines_status() {
        assertTrue(p.getState() instanceof ProviderState);
    }

    @Specification(
            number = "2.4.3",
            text =
                    "The provider MUST set its status field/accessor to READY if its initialize function terminates normally.")
    @Specification(
            number = "2.4.4",
            text = "The provider MUST set its status field to ERROR if its initialize function terminates abnormally.")
    @Specification(
            number = "2.2.9",
            text = "The provider SHOULD populate the resolution details structure's flag metadata field.")
    @Specification(
            number = "2.4.1",
            text =
                    "The provider MAY define an initialize function which accepts the global evaluation context as an argument and performs initialization logic relevant to the provider.")
    @Specification(
            number = "2.5.1",
            text = "The provider MAY define a mechanism to gracefully shutdown and dispose of resources.")
    @Test
    void provider_responsibility() {}

    @Specification(
            number = "2.6.1",
            text =
                    "The provider MAY define an on context changed handler, which takes an argument for the previous context and the newly set context, in order to respond to an evaluation context change.")
    @Test
    void not_applicable_for_dynamic_context() {}
}

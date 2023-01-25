package dev.openfeature.sdk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ProviderSpecTest {
    NoOpProvider p = new NoOpProvider();

    @Specification(number="2.1.1", text="The provider interface MUST define a metadata member or accessor, containing a name field or accessor of type string, which identifies the provider implementation.")
    @Test void name_accessor() {
        assertNotNull(p.getName());
    }

    @Specification(number="2.2.2.1", text="The feature provider interface MUST define methods for typed " +
            "flag resolution, including boolean, numeric, string, and structure.")
    @Specification(number="2.2.3", text="In cases of normal execution, the `provider` MUST populate the `resolution details` structure's `value` field with the resolved flag value.")
    @Specification(number="2.2.1", text="The `feature provider` interface MUST define methods to resolve flag values, with parameters `flag key` (string, required), `default value` (boolean | number | string | structure, required) and `evaluation context` (optional), which returns a `resolution details` structure.")
    @Specification(number="2.2.8.1", text="The `resolution details` structure SHOULD accept a generic argument (or use an equivalent language feature) which indicates the type of the wrapped `value` field.")
    @Test void flag_value_set() {
        ProviderEvaluation<Integer> int_result = p.getIntegerEvaluation("key", 4, new MutableContext());
        assertNotNull(int_result.getValue());

        ProviderEvaluation<Double> double_result = p.getDoubleEvaluation("key", 0.4, new MutableContext());
        assertNotNull(double_result.getValue());

        ProviderEvaluation<String> string_result = p.getStringEvaluation("key", "works", new MutableContext());
        assertNotNull(string_result.getValue());

        ProviderEvaluation<Boolean> boolean_result = p.getBooleanEvaluation("key", false, new MutableContext());
        assertNotNull(boolean_result.getValue());

        ProviderEvaluation<Value> object_result = p.getObjectEvaluation("key", new Value(), new MutableContext());
        assertNotNull(object_result.getValue());

    }

    @Specification(number="2.2.5", text="The `provider` SHOULD populate the `resolution details` structure's `reason` field with `\"STATIC\"`, `\"DEFAULT\",` `\"TARGETING_MATCH\"`, `\"SPLIT\"`, `\"CACHED\"`, `\"DISABLED\"`, `\"UNKNOWN\"`, `\"ERROR\"` or some other string indicating the semantic reason for the returned flag value.")
    @Test void has_reason() {
        ProviderEvaluation<Boolean> result = p.getBooleanEvaluation("key", false, new MutableContext());
        assertEquals(Reason.DEFAULT.toString(), result.getReason());
    }

    @Specification(number="2.2.6", text="In cases of normal execution, the `provider` MUST NOT populate the `resolution details` structure's `error code` field, or otherwise must populate it with a null or falsy value.")
    @Test void no_error_code_by_default() {
        ProviderEvaluation<Boolean> result = p.getBooleanEvaluation("key", false, new MutableContext());
        assertNull(result.getErrorCode());
    }

    @Specification(number="2.2.7", text="In cases of abnormal execution, the `provider` **MUST** indicate an error using the idioms of the implementation language, with an associated `error code` and optional associated `error message`.")
    @Specification(number="2.3.2", text="In cases of normal execution, the `provider` MUST NOT populate the `resolution details` structure's `error message` field, or otherwise must populate it with a null or falsy value.")
    @Specification(number="2.3.3", text="In cases of abnormal execution, the `resolution details` structure's `error message` field MAY contain a string containing additional detail about the nature of the error.")
    @Test void up_to_provider_implementation() {}

    @Specification(number="2.2.4", text="In cases of normal execution, the `provider` SHOULD populate the `resolution details` structure's `variant` field with a string identifier corresponding to the returned flag value.")
    @Test void variant_set() {
        ProviderEvaluation<Integer> int_result = p.getIntegerEvaluation("key", 4, new MutableContext());
        assertNotNull(int_result.getReason());

        ProviderEvaluation<Double> double_result = p.getDoubleEvaluation("key", 0.4, new MutableContext());
        assertNotNull(double_result.getReason());

        ProviderEvaluation<String> string_result = p.getStringEvaluation("key", "works", new MutableContext());
        assertNotNull(string_result.getReason());

        ProviderEvaluation<Boolean> boolean_result = p.getBooleanEvaluation("key", false, new MutableContext());
        assertNotNull(boolean_result.getReason());
    }

    @Specification(number="2.3.1", text="The provider interface MUST define a provider hook mechanism which can be optionally implemented in order to add hook instances to the evaluation life-cycle.")
    @Specification(number="4.4.1", text="The API, Client, Provider, and invocation MUST have a method for registering hooks.")
    @Test void provider_hooks() {
        assertEquals(0, p.getProviderHooks().size());
    }
}

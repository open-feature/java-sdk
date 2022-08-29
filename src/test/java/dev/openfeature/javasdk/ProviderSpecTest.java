package dev.openfeature.javasdk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ProviderSpecTest {
    NoOpProvider p = new NoOpProvider();

    @Specification(number="2.1", text="The provider interface MUST define a metadata member or accessor, containing a name field or accessor of type string, which identifies the provider implementation.")
    @Test void name_accessor() {
        assertNotNull(p.getName());
    }

    @Specification(number="2.3.1", text="The feature provider interface MUST define methods for typed " +
            "flag resolution, including boolean, numeric, string, and structure.")
    @Specification(number="2.4", text="In cases of normal execution, the provider MUST populate the " +
            "flag resolution structure's value field with the resolved flag value.")
    @Specification(number="2.2", text="The feature provider interface MUST define methods to resolve " +
            "flag values, with parameters flag key (string, required), default value " +
            "(boolean | number | string | structure, required) and evaluation context (optional), " +
            "which returns a flag resolution structure.")
    @Specification(number="2.9.1", text="The flag resolution structure SHOULD accept a generic " +
            "argument (or use an equivalent language feature) which indicates the type of the wrapped value field.")
    @Test void flag_value_set() {
        ProviderEvaluation<Integer> int_result = p.getIntegerEvaluation("key", 4, new EvaluationContext());
        assertNotNull(int_result.getValue());

        ProviderEvaluation<Double> double_result = p.getDoubleEvaluation("key", 0.4, new EvaluationContext());
        assertNotNull(double_result.getValue());

        ProviderEvaluation<String> string_result = p.getStringEvaluation("key", "works", new EvaluationContext());
        assertNotNull(string_result.getValue());

        ProviderEvaluation<Boolean> boolean_result = p.getBooleanEvaluation("key", false, new EvaluationContext());
        assertNotNull(boolean_result.getValue());

        ProviderEvaluation<Structure> object_result = p.getObjectEvaluation("key", new Structure(), new EvaluationContext());
        assertNotNull(object_result.getValue());

    }

    @Specification(number="2.6", text="The provider SHOULD populate the flag resolution structure's " +
            "reason field with a string indicating the semantic reason for the returned flag value.")
    @Test void has_reason() {
        ProviderEvaluation<Boolean> result = p.getBooleanEvaluation("key", false, new EvaluationContext());
        assertEquals(Reason.DEFAULT, result.getReason());
    }

    @Specification(number="2.7", text="In cases of normal execution, the provider MUST NOT populate " +
            "the flag resolution structure's error code field, or otherwise must populate it with a null or falsy value.")
    @Test void no_error_code_by_default() {
        ProviderEvaluation<Boolean> result = p.getBooleanEvaluation("key", false, new EvaluationContext());
        assertNull(result.getErrorCode());
    }

    @Specification(number="2.8", text="In cases of abnormal execution, the provider MUST indicate an " +
    "error using the idioms of the implementation language, with an associated error code having possible " +
    "values PROVIDER_NOT_READY, FLAG_NOT_FOUND, PARSE_ERROR, TYPE_MISMATCH, or GENERAL.")
    @Test void up_to_provider_implementation() {}

    @Specification(number="2.5", text="In cases of normal execution, the provider SHOULD populate the " +
            "flag resolution structure's variant field with a string identifier corresponding to the returned flag value.")
    @Test void variant_set() {
        ProviderEvaluation<Integer> int_result = p.getIntegerEvaluation("key", 4, new EvaluationContext());
        assertNotNull(int_result.getReason());

        ProviderEvaluation<Double> double_result = p.getDoubleEvaluation("key", 0.4, new EvaluationContext());
        assertNotNull(double_result.getReason());

        ProviderEvaluation<String> string_result = p.getStringEvaluation("key", "works", new EvaluationContext());
        assertNotNull(string_result.getReason());

        ProviderEvaluation<Boolean> boolean_result = p.getBooleanEvaluation("key", false, new EvaluationContext());
        assertNotNull(boolean_result.getReason());
    }

    @Specification(number="2.10", text="The provider interface MUST define a provider hook mechanism which can be optionally implemented in order to add hook instances to the evaluation life-cycle.")
    @Specification(number="4.4.1", text="The API, Client, Provider, and invocation MUST have a method for registering hooks.")
    @Test void provider_hooks() {
        assertEquals(0, p.getProviderHooks().size());
    }
}

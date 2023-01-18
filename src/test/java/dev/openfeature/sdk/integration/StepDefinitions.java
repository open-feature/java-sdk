package dev.openfeature.sdk.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import dev.openfeature.contrib.providers.flagd.FlagdProvider;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.Reason;
import dev.openfeature.sdk.Structure;
import dev.openfeature.sdk.Value;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;

import java.io.File;
import java.io.FileWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StepDefinitions {

    private static JsonNode flagConfig;
    private static String testingFlagsPath = "test-harness/testing-flags.json";
    private static Client client;
    private boolean booleanFlagValue;
    private String stringFlagValue;
    private int intFlagValue;
    private double doubleFlagValue;
    private Value objectFlagValue;

    private Map<String, FlagEvaluationDetails<Boolean>> booleanFlagDetailsMap = new HashMap<String, FlagEvaluationDetails<Boolean>>();
    private Map<String, FlagEvaluationDetails<String>> stringFlagDetailsMap = new HashMap<String, FlagEvaluationDetails<String>>();
    private Map<String, FlagEvaluationDetails<Integer>> intFlagDetailsMap = new HashMap<String, FlagEvaluationDetails<Integer>>();
    private Map<String, FlagEvaluationDetails<Double>> doubleFlagDetailsMap = new HashMap<String, FlagEvaluationDetails<Double>>();
    private Map<String, FlagEvaluationDetails<Value>> objectFlagDetailsMap = new HashMap<String, FlagEvaluationDetails<Value>>();

    private String contextAwareFlagKey;
    private String contextAwareDefaultValue;
    private MutableContext context;
    private String contextAwareValue;

    private String notFoundFlagKey;
    private String notFoundDefaultValue;
    private FlagEvaluationDetails<String> notFoundDetails;
    private int typeErrorDefaultValue;
    private FlagEvaluationDetails<Integer> typeErrorDetails;

    @BeforeAll
    public static void init() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            flagConfig = mapper.readTree(new File(testingFlagsPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public static void reset_state() {
        try {
            FileWriter file = new FileWriter(testingFlagsPath);
            file.write(flagConfig.toString());
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Given("a provider is registered with cache disabled")
    public static void a_provider_is_registered_with_cache_disabled() {
        FlagdProvider provider = new FlagdProvider(null, 8013, false, null, null, 0, 0);
        provider.setDeadline(3000); // set a generous deadline, to prevent timeouts in actions
        OpenFeatureAPI.getInstance().setProvider(provider);
        client = OpenFeatureAPI.getInstance().getClient();
    }

    /*
     * Basic evaluation
     */

    // boolean value
    @When("a boolean flag with key {string} is evaluated with default value {string}")
    public void a_boolean_flag_with_key_boolean_flag_is_evaluated_with_default_value_false(String flagKey,
            String defaultValue) {
        this.booleanFlagValue = client.getBooleanValue(flagKey, Boolean.valueOf(defaultValue));
    }

    @Then("the resolved boolean value should be {string}")
    public void the_resolved_boolean_value_should_be_true(String expected) {
        assertEquals(Boolean.valueOf(expected), this.booleanFlagValue);
    }

    // string value
    @When("a string flag with key {string} is evaluated with default value {string}")
    public void a_string_flag_with_key_is_evaluated_with_default_value(String flagKey, String defaultValue) {
        this.stringFlagValue = client.getStringValue(flagKey, defaultValue);
    }

    @Then("the resolved string value should be {string}")
    public void the_resolved_string_value_should_be(String expected) {
        assertEquals(expected, this.stringFlagValue);
    }

    // integer value
    @When("an integer flag with key {string} is evaluated with default value {int}")
    public void an_integer_flag_with_key_is_evaluated_with_default_value(String flagKey, Integer defaultValue) {
        this.intFlagValue = client.getIntegerValue(flagKey, defaultValue);
    }

    @Then("the resolved integer value should be {int}")
    public void the_resolved_integer_value_should_be(int expected) {
        assertEquals(expected, this.intFlagValue);
    }

    // float/double value
    @When("a float flag with key {string} is evaluated with default value {double}")
    public void a_float_flag_with_key_is_evaluated_with_default_value(String flagKey, double defaultValue) {
        this.doubleFlagValue = client.getDoubleValue(flagKey, defaultValue);
    }

    @Then("the resolved float value should be {double}")
    public void the_resolved_float_value_should_be(double expected) {
        assertEquals(expected, this.doubleFlagValue);
    }

    // object value
    @When("an object flag with key {string} is evaluated with a null default value")
    public void an_object_flag_with_key_is_evaluated_with_a_null_default_value(String flagKey) {
        this.objectFlagValue = client.getObjectValue(flagKey, new Value());
    }

    @Then("the resolved object value should be contain fields {string}, {string}, and {string}, with values {string}, {string} and {int}, respectively")
    public void the_resolved_object_value_should_be_contain_fields_and_with_values_and_respectively(String boolField,
            String stringField, String numberField, String boolValue, String stringValue, int numberValue) {
        Structure structure = this.objectFlagValue.asStructure();

        assertEquals(Boolean.valueOf(boolValue), structure.asMap().get(boolField).asBoolean());
        assertEquals(stringValue, structure.asMap().get(stringField).asString());
        assertEquals(numberValue, structure.asMap().get(numberField).asInteger());
    }

    /*
     * Detailed evaluation
     */

    // boolean details
    @Given("a boolean flag with key {string} is evaluated with details and default value {string}")
    public void a_boolean_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey,
            String defaultValue) {
        booleanFlagDetailsMap.put(flagKey, client.getBooleanDetails(flagKey, Boolean.valueOf(defaultValue)));
    }

    @Then("the resolved boolean details value should be {string}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_boolean_value_should_be_the_variant_should_be_and_the_reason_should_be(
            String expectedValue,
            String expectedVariant, String expectedReason) {
        FlagEvaluationDetails<Boolean> booleanFlagDetails = firstEvaluationDetails(booleanFlagDetailsMap);
        assertEquals(Boolean.valueOf(expectedValue), booleanFlagDetails.getValue());
        assertEquals(expectedVariant, booleanFlagDetails.getVariant());
        assertEquals(expectedReason, booleanFlagDetails.getReason());
    }

    // string details
    @Given("a string flag with key {string} is evaluated with details and default value {string}")
    public void a_string_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey,
            String defaultValue) {
        stringFlagDetailsMap.put(flagKey, client.getStringDetails(flagKey, defaultValue));
    }

    @Then("the resolved string details value should be {string}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_string_value_should_be_the_variant_should_be_and_the_reason_should_be(String expectedValue,
            String expectedVariant, String expectedReason) {
        FlagEvaluationDetails<String> stringFlagDetails = firstEvaluationDetails(stringFlagDetailsMap);
        assertEquals(expectedValue, stringFlagDetails.getValue());
        assertEquals(expectedVariant, stringFlagDetails.getVariant());
        assertEquals(expectedReason, stringFlagDetails.getReason());
    }

    // integer details
    @Given("an integer flag with key {string} is evaluated with details and default value {int}")
    public void an_integer_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey, int defaultValue) {
        intFlagDetailsMap.put(flagKey, client.getIntegerDetails(flagKey, defaultValue));
    }

    @Then("the resolved integer details value should be {int}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_integer_value_should_be_the_variant_should_be_and_the_reason_should_be(int expectedValue,
            String expectedVariant, String expectedReason) {
        FlagEvaluationDetails<Integer> intFlagDetails = firstEvaluationDetails(intFlagDetailsMap);
        assertEquals(expectedValue, intFlagDetails.getValue());
        assertEquals(expectedVariant, intFlagDetails.getVariant());
        assertEquals(expectedReason, intFlagDetails.getReason());
    }

    // float/double details
    @Given("a float flag with key {string} is evaluated with details and default value {double}")
    public void a_float_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey, double defaultValue) {
        doubleFlagDetailsMap.put(flagKey, client.getDoubleDetails(flagKey, defaultValue));
    }

    @Then("the resolved float details value should be {double}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_float_value_should_be_the_variant_should_be_and_the_reason_should_be(double expectedValue,
            String expectedVariant, String expectedReason) {
        FlagEvaluationDetails<Double> doubleFlagDetails = firstEvaluationDetails(doubleFlagDetailsMap);
        assertEquals(expectedValue, doubleFlagDetails.getValue());
        assertEquals(expectedVariant, doubleFlagDetails.getVariant());
        assertEquals(expectedReason, doubleFlagDetails.getReason());
    }

    // object details
    @Given("an object flag with key {string} is evaluated with details and a null default value")
    public void an_object_flag_with_key_is_evaluated_with_details_and_a_null_default_value(String flagKey) {
        objectFlagDetailsMap.put(flagKey, client.getObjectDetails(flagKey, new Value()));
    }

    @Then("the resolved object details value should be contain fields {string}, {string}, and {string}, with values {string}, {string} and {int}, respectively")
    public void the_resolved_object_value_should_be_contain_fields_and_with_values_and_respectively_again(
            String boolField,
            String stringField, String numberField, String boolValue, String stringValue, int numberValue) {
        FlagEvaluationDetails<Value> objectFlagDetails = firstEvaluationDetails(objectFlagDetailsMap);
        Structure structure = objectFlagDetails.getValue().asStructure();

        assertEquals(Boolean.valueOf(boolValue), structure.asMap().get(boolField).asBoolean());
        assertEquals(stringValue, structure.asMap().get(stringField).asString());
        assertEquals(numberValue, structure.asMap().get(numberField).asInteger());
    }

    @Then("the variant should be {string}, and the reason should be {string}")
    public void the_variant_should_be_and_the_reason_should_be(String expectedVariant, String expectedReason) {
        FlagEvaluationDetails<Value> objectFlagDetails = firstEvaluationDetails(objectFlagDetailsMap);
        assertEquals(expectedVariant, objectFlagDetails.getVariant());
        assertEquals(expectedReason, objectFlagDetails.getReason());
    }

    /*
     * Context-aware evaluation
     */

    @When("context contains keys {string}, {string}, {string}, {string} with values {string}, {string}, {int}, {string}")
    public void context_contains_keys_with_values(String field1, String field2, String field3, String field4,
            String value1, String value2, Integer value3, String value4) {
        this.context = new MutableContext()
                .add(field1, value1)
                .add(field2, value2)
                .add(field3, value3)
                .add(field4, Boolean.valueOf(value4));
    }

    @When("a flag with key {string} is evaluated with default value {string}")
    public void an_a_flag_with_key_is_evaluated(String flagKey, String defaultValue) {
        contextAwareFlagKey = flagKey;
        contextAwareDefaultValue = defaultValue;
        contextAwareValue = client.getStringValue(flagKey, contextAwareDefaultValue, context);

    }

    @Then("the resolved string response should be {string}")
    public void the_resolved_string_response_should_be(String expected) {
        assertEquals(expected, this.contextAwareValue);
    }

    @Then("the resolved flag value is {string} when the context is empty")
    public void the_resolved_flag_value_is_when_the_context_is_empty(String expected) {
        String emptyContextValue = client.getStringValue(contextAwareFlagKey, contextAwareDefaultValue,
                new MutableContext());
        assertEquals(expected, emptyContextValue);
    }

    /*
     * Errors
     */

    // not found
    @When("a non-existent string flag with key {string} is evaluated with details and a default value {string}")
    public void a_non_existent_string_flag_with_key_is_evaluated_with_details_and_a_default_value(String flagKey,
            String defaultValue) {
        notFoundFlagKey = flagKey;
        notFoundDefaultValue = defaultValue;
        notFoundDetails = client.getStringDetails(notFoundFlagKey, notFoundDefaultValue);
    }

    @Then("the default string value should be returned")
    public void then_the_default_string_value_should_be_returned() {
        assertEquals(notFoundDefaultValue, notFoundDetails.getValue());
    }

    @Then("the reason should indicate an error and the error code should indicate a missing flag with {string}")
    public void the_reason_should_indicate_an_error_and_the_error_code_should_be_flag_not_found(String errorCode) {
        assertEquals(Reason.ERROR.toString(), notFoundDetails.getReason());
        assertTrue(notFoundDetails.getErrorMessage().contains(errorCode));
        // TODO: add errorCode assertion once flagd provider is updated.
    }

    @Then("the default integer value should be returned")
    public void then_the_default_integer_value_should_be_returned() {
        assertEquals(typeErrorDefaultValue, typeErrorDetails.getValue());
    }

    @Then("the reason should indicate an error and the error code should indicate a type mismatch with {string}")
    public void the_reason_should_indicate_an_error_and_the_error_code_should_be_type_mismatch(String errorCode) {
        assertEquals(Reason.ERROR.toString(), typeErrorDetails.getReason());
        assertTrue(typeErrorDetails.getErrorMessage().contains(errorCode));
        // TODO: add errorCode assertion once flagd provider is updated.
    }

    @Given("a provider is registered with cache enabled")
    public static void a_provider_is_registered_with_cache_enabled() {
        FlagdProvider provider = new FlagdProvider(); // cache enabled by default
        provider.setDeadline(3000); // set a generous deadline, to prevent timeouts in actions
        OpenFeatureAPI.getInstance().setProvider(provider);
        client = OpenFeatureAPI.getInstance().getClient();

        Object eventStreamAliveSync = provider.getEventStreamAliveSync();
        synchronized (eventStreamAliveSync) {
            try {
                eventStreamAliveSync.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Then("the resolved boolean details reason should be {string}")
    public void the_resolved_boolean_details_reason_should_be(String expectedReason) {
        FlagEvaluationDetails<Boolean> booleanFlagDetails = firstEvaluationDetails(booleanFlagDetailsMap);
        assertEquals(expectedReason, booleanFlagDetails.getReason());
    }

    @Then("the resolved string details reason should be {string}")
    public void the_resolved_string_details_reason_should_be(String expectedReason) {
        FlagEvaluationDetails<String> stringFlagDetails = firstEvaluationDetails(stringFlagDetailsMap);
        assertEquals(expectedReason, stringFlagDetails.getReason());
    }

    @Then("the resolved integer details reason should be {string}")
    public void the_resolved_integer_details_reason_should_be(String expectedReason) {
        FlagEvaluationDetails<Integer> intFlagDetails = firstEvaluationDetails(intFlagDetailsMap);
        assertEquals(expectedReason, intFlagDetails.getReason());
    }

    @Then("the resolved float details reason should be {string}")
    public void the_resolved_float_details_reason_should_be(String expectedReason) {
        FlagEvaluationDetails<Double> doubleFlagDetails = firstEvaluationDetails(doubleFlagDetailsMap);
        assertEquals(expectedReason, doubleFlagDetails.getReason());
    }

    @Then("the resolved object details reason should be {string}")
    public void the_resolved_object_details_reason_should_be(String expectedReason) {
        FlagEvaluationDetails<Value> objectFlagDetails = firstEvaluationDetails(objectFlagDetailsMap);
        assertEquals(expectedReason, objectFlagDetails.getReason());
    }

    @When("the flag's configuration with key {string} is updated to defaultVariant {string}")
    public void the_flags_configuration_with_key_is_updated_to_defaultVariant(String flagKey, String defaultVariant) {
        JsonNode copy = flagConfig.deepCopy();
        JsonNode flags = copy.get("flags");
        JsonNode flag = flags.get(flagKey);
        ((ObjectNode)flag).put("defaultVariant", defaultVariant);

        try {
            FileWriter file = new FileWriter(testingFlagsPath);
            file.write(copy.toString());
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @And("sleep for {int} milliseconds")
    public void sleep_for_milliseconds(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("the resolved boolean details reason of flag with key {string} should be {string}")
    public void the_resolved_boolean_details_reason_of_flag_with_key_should_be(String flagKey, String expectedReason) {
        FlagEvaluationDetails<Boolean> stringFlagDetails = booleanFlagDetailsMap.get(flagKey);
        assertEquals(expectedReason, stringFlagDetails.getReason());
    }

    @Then("the resolved string details reason of flag with key {string} should be {string}")
    public void the_resolved_string_details_reason_of_flag_with_key_should_be(String flagKey, String expectedReason) {
        FlagEvaluationDetails<String> stringFlagDetails = stringFlagDetailsMap.get(flagKey);
        assertEquals(expectedReason, stringFlagDetails.getReason());
    }

    @Then("the resolved integer details reason of flag with key {string} should be {string}")
    public void the_resolved_integer_details_reason_of_flag_with_key_should_be(String flagKey, String expectedReason) {
        FlagEvaluationDetails<Integer> intFlagDetails = intFlagDetailsMap.get(flagKey);
        assertEquals(expectedReason, intFlagDetails.getReason());
    }

    @Then("the resolved float details reason of flag with key {string} should be {string}")
    public void the_resolved_float_details_reason_of_flag_with_key_should_be(String flagKey, String expectedReason) {
        FlagEvaluationDetails<Double> doubleFlagDetails = doubleFlagDetailsMap.get(flagKey);
        assertEquals(expectedReason, doubleFlagDetails.getReason());
    }

    @Then("the resolved object details reason of flag with key {string} should be {string}")
    public void the_resolved_object_details_reason_of_flag_with_key_should_be(String flagKey, String expectedReason) {
        FlagEvaluationDetails<Value> objectFlagDetails = objectFlagDetailsMap.get(flagKey);
        assertEquals(expectedReason, objectFlagDetails.getReason());
    }

    @When("a string flag with key {string} is evaluated as an integer, with details and a default value {int}")
    public void a_string_flag_with_key_is_evaluated_as_an_integer_with_details_and_a_default_value(String flagKey, Integer defaultValue) {
        typeErrorDefaultValue = defaultValue;
        typeErrorDetails = client.getIntegerDetails(flagKey, defaultValue);
    }

    private static <T> FlagEvaluationDetails<T> firstEvaluationDetails(Map<String, FlagEvaluationDetails<T>> flagsMap) {
        for (String key: flagsMap.keySet()) {
            return flagsMap.get(key);
        }

        return null;
    }
}

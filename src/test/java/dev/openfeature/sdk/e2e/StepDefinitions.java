package dev.openfeature.sdk.e2e;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.sdk.*;
import dev.openfeature.sdk.providers.memory.ContextEvaluator;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.Flags;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

import static dev.openfeature.sdk.providers.memory.InMemoryProvider.mapToStructure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StepDefinitions {

    private static Client client;
    private boolean booleanFlagValue;
    private String stringFlagValue;
    private int intFlagValue;
    private double doubleFlagValue;
    private Value objectFlagValue;

    private FlagEvaluationDetails<Boolean> booleanFlagDetails;
    private FlagEvaluationDetails<String> stringFlagDetails;
    private FlagEvaluationDetails<Integer> intFlagDetails;
    private FlagEvaluationDetails<Double> doubleFlagDetails;
    private FlagEvaluationDetails<Value> objectFlagDetails;

    private String contextAwareFlagKey;
    private String contextAwareDefaultValue;
    private EvaluationContext context;
    private String contextAwareValue;

    private String notFoundFlagKey;
    private String notFoundDefaultValue;
    private FlagEvaluationDetails<String> notFoundDetails;
    private String typeErrorFlagKey;
    private int typeErrorDefaultValue;
    private FlagEvaluationDetails<Integer> typeErrorDetails;

    @SneakyThrows
    @BeforeAll()
    @Given("an openfeature client is registered with cache disabled")
    public static void setup() {
        Flags flags = Flags.builder()
            .flag("boolean-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("on", true)
                .variant("off", false)
                .defaultVariant("on")
                .build())
            .flag("string-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("greeting", "hi")
                .variant("parting", "bye")
                .defaultVariant("greeting")
                .build())
            .flag("integer-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("one",  1)
                .variant("ten", 10)
                .defaultVariant("ten")
                .build())
            .flag("float-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("tenth", 0.1)
                .variant("half", 0.5)
                .defaultVariant("half")
                .build())
            .flag("object-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("empty", new HashMap<>())
                .variant("template", new Value(mapToStructure(ImmutableMap.of(
                    "showImages", new Value(true),
                    "title", new Value("Check out these pics!"),
                    "imagesPerPage", new Value(100)
                ))))
                .defaultVariant("template")
                .build())
            .flag("context-aware", Flag.<String>builder().state(Flag.State.ENABLED)
                .variant("internal", "INTERNAL")
                .variant("external", "EXTERNAL")
                .defaultVariant("external")
                .contextEvaluator((ContextEvaluator<String>) (flag, evaluationContext) -> {
                    if (new Value(false).equals(evaluationContext.getValue("customer"))) {
                        return (String) flag.getVariants().get("internal");
                    } else {
                        return (String) flag.getVariants().get(flag.getDefaultVariant());
                    }
                })
                .build())
            .flag("wrong-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("one",  "uno")
                .variant("two", "dos")
                .defaultVariant("one")
                .build())
            .build();
        InMemoryProvider provider = new InMemoryProvider(flags);
        OpenFeatureAPI.getInstance().setProvider(provider);

        // TODO: setProvider with wait for init, pending https://github.com/open-feature/ofep/pull/80
        Thread.sleep(500);

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
    @When("a boolean flag with key {string} is evaluated with details and default value {string}")
    public void a_boolean_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey,
            String defaultValue) {
        this.booleanFlagDetails = client.getBooleanDetails(flagKey, Boolean.valueOf(defaultValue));
    }

    @Then("the resolved boolean details value should be {string}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_boolean_value_should_be_the_variant_should_be_and_the_reason_should_be(
            String expectedValue,
            String expectedVariant, String expectedReason) {
        assertEquals(Boolean.valueOf(expectedValue), booleanFlagDetails.getValue());
        assertEquals(expectedVariant, booleanFlagDetails.getVariant());
        assertEquals(expectedReason, booleanFlagDetails.getReason());
    }

    // string details
    @When("a string flag with key {string} is evaluated with details and default value {string}")
    public void a_string_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey,
            String defaultValue) {
        this.stringFlagDetails = client.getStringDetails(flagKey, defaultValue);
    }

    @Then("the resolved string details value should be {string}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_string_value_should_be_the_variant_should_be_and_the_reason_should_be(String expectedValue,
            String expectedVariant, String expectedReason) {
        assertEquals(expectedValue, this.stringFlagDetails.getValue());
        assertEquals(expectedVariant, this.stringFlagDetails.getVariant());
        assertEquals(expectedReason, this.stringFlagDetails.getReason());
    }

    // integer details
    @When("an integer flag with key {string} is evaluated with details and default value {int}")
    public void an_integer_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey, int defaultValue) {
        this.intFlagDetails = client.getIntegerDetails(flagKey, defaultValue);
    }

    @Then("the resolved integer details value should be {int}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_integer_value_should_be_the_variant_should_be_and_the_reason_should_be(int expectedValue,
            String expectedVariant, String expectedReason) {
        assertEquals(expectedValue, this.intFlagDetails.getValue());
        assertEquals(expectedVariant, this.intFlagDetails.getVariant());
        assertEquals(expectedReason, this.intFlagDetails.getReason());
    }

    // float/double details
    @When("a float flag with key {string} is evaluated with details and default value {double}")
    public void a_float_flag_with_key_is_evaluated_with_details_and_default_value(String flagKey, double defaultValue) {
        this.doubleFlagDetails = client.getDoubleDetails(flagKey, defaultValue);
    }

    @Then("the resolved float details value should be {double}, the variant should be {string}, and the reason should be {string}")
    public void the_resolved_float_value_should_be_the_variant_should_be_and_the_reason_should_be(double expectedValue,
            String expectedVariant, String expectedReason) {
        assertEquals(expectedValue, this.doubleFlagDetails.getValue());
        assertEquals(expectedVariant, this.doubleFlagDetails.getVariant());
        assertEquals(expectedReason, this.doubleFlagDetails.getReason());
    }

    // object details
    @When("an object flag with key {string} is evaluated with details and a null default value")
    public void an_object_flag_with_key_is_evaluated_with_details_and_a_null_default_value(String flagKey) {
        this.objectFlagDetails = client.getObjectDetails(flagKey, new Value());
    }

    @Then("the resolved object details value should be contain fields {string}, {string}, and {string}, with values {string}, {string} and {int}, respectively")
    public void the_resolved_object_value_should_be_contain_fields_and_with_values_and_respectively_again(
            String boolField,
            String stringField, String numberField, String boolValue, String stringValue, int numberValue) {
        Structure structure = this.objectFlagDetails.getValue().asStructure();

        assertEquals(Boolean.valueOf(boolValue), structure.asMap().get(boolField).asBoolean());
        assertEquals(stringValue, structure.asMap().get(stringField).asString());
        assertEquals(numberValue, structure.asMap().get(numberField).asInteger());
    }

    @Then("the variant should be {string}, and the reason should be {string}")
    public void the_variant_should_be_and_the_reason_should_be(String expectedVariant, String expectedReason) {
        assertEquals(expectedVariant, this.objectFlagDetails.getVariant());
        assertEquals(expectedReason, this.objectFlagDetails.getReason());
    }

    /*
     * Context-aware evaluation
     */

    @When("context contains keys {string}, {string}, {string}, {string} with values {string}, {string}, {int}, {string}")
    public void context_contains_keys_with_values(String field1, String field2, String field3, String field4,
            String value1, String value2, Integer value3, String value4) {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put(field1, new Value(value1));
        attributes.put(field2, new Value(value2));
        attributes.put(field3, new Value(value3));
        attributes.put(field4, new Value(Boolean.valueOf(value4)));
        this.context = new ImmutableContext(attributes);
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
                new ImmutableContext());
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
        assertTrue(notFoundDetails.getErrorCode().name().equals(errorCode));
    }

    // type mismatch
    @When("a string flag with key {string} is evaluated as an integer, with details and a default value {int}")
    public void a_string_flag_with_key_is_evaluated_as_an_integer_with_details_and_a_default_value(String flagKey,
            int defaultValue) {
        typeErrorFlagKey = flagKey;
        typeErrorDefaultValue = defaultValue;
        typeErrorDetails = client.getIntegerDetails(typeErrorFlagKey, typeErrorDefaultValue);
    }

    @Then("the default integer value should be returned")
    public void then_the_default_integer_value_should_be_returned() {
        assertEquals(typeErrorDefaultValue, typeErrorDetails.getValue());
    }

    @Then("the reason should indicate an error and the error code should indicate a type mismatch with {string}")
    public void the_reason_should_indicate_an_error_and_the_error_code_should_be_type_mismatch(String errorCode) {
        assertEquals(Reason.ERROR.toString(), typeErrorDetails.getReason());
        assertTrue(typeErrorDetails.getErrorMessage().contains(errorCode));
        assertTrue(typeErrorDetails.getErrorCode().name().equals(errorCode));
    }

}

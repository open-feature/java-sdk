package dev.openfeature.sdk.e2e.steps;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.api.Metadata;
import dev.openfeature.api.Value;
import dev.openfeature.sdk.e2e.Flag;
import dev.openfeature.sdk.e2e.State;
import dev.openfeature.sdk.e2e.Utils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class FlagStepDefinitions {
    private final State state;

    public FlagStepDefinitions(State state) {
        this.state = state;
    }

    @Given("a {}-flag with key {string} and a default value {string}")
    public void givenAFlag(String type, String name, String defaultValue) {
        state.flag = new Flag(type, name, Utils.convert(defaultValue, type));
    }

    @When("the flag was evaluated with details")
    public void the_flag_was_evaluated_with_details() {
        FlagEvaluationDetails details;
        switch (state.flag.type.toLowerCase()) {
            case "string":
                details =
                        state.client.getStringDetails(state.flag.name, (String) state.flag.defaultValue, state.context);
                break;
            case "boolean":
                details = state.client.getBooleanDetails(
                        state.flag.name, (Boolean) state.flag.defaultValue, state.context);
                break;
            case "float":
                details =
                        state.client.getDoubleDetails(state.flag.name, (Double) state.flag.defaultValue, state.context);
                break;
            case "integer":
                details = state.client.getIntegerDetails(
                        state.flag.name, (Integer) state.flag.defaultValue, state.context);
                break;
            case "object":
                details =
                        state.client.getObjectDetails(state.flag.name, (Value) state.flag.defaultValue, state.context);
                break;
            default:
                throw new AssertionError();
        }
        state.evaluation = details;
    }

    @Then("the resolved details value should be {string}")
    public void the_resolved_details_value_should_be(String value) {
        assertThat(state.evaluation.getValue()).isEqualTo(Utils.convert(value, state.flag.type));
    }

    @Then("the reason should be {string}")
    public void the_reason_should_be(String reason) {
        assertThat(state.evaluation.getReason()).isEqualTo(reason);
    }

    @Then("the variant should be {string}")
    public void the_variant_should_be(String variant) {
        assertThat(state.evaluation.getVariant()).isEqualTo(variant);
    }

    @Then("the resolved metadata value \"{}\" with type \"{}\" should be \"{}\"")
    public void theResolvedMetadataValueShouldBe(String key, String type, String value)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = state.evaluation.getFlagMetadata().getClass().getDeclaredField("metadata");
        f.setAccessible(true);
        HashMap<String, Object> metadata = (HashMap<String, Object>) f.get(state.evaluation.getFlagMetadata());
        assertThat(metadata).containsEntry(key, Utils.convert(value, type));
    }

    @Then("the resolved metadata is empty")
    public void theResolvedMetadataIsEmpty() {
        assertThat(state.evaluation.getFlagMetadata().isEmpty()).isTrue();
    }

    @Then("the resolved metadata should contain")
    public void theResolvedMetadataShouldContain(DataTable dataTable) {
        Metadata evaluationMetadata = state.evaluation.getFlagMetadata();
        List<List<String>> asLists = dataTable.asLists();
        for (int i = 1; i < asLists.size(); i++) { // skip the header of the table
            List<String> line = asLists.get(i);
            String key = line.get(0);
            String metadataType = line.get(1);
            Object value = Utils.convert(line.get(2), metadataType);

            assertThat(value).isNotNull();
            assertThat(evaluationMetadata.getValue(key, value.getClass())).isEqualTo(value);
        }
    }
}

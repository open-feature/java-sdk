package dev.openfeature.sdk.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.sdk.e2e.MockHook;
import dev.openfeature.sdk.e2e.State;
import dev.openfeature.sdk.e2e.Utils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.List;
import java.util.Map;

public class HookSteps {
    private final State state;

    public HookSteps(State state) {
        this.state = state;
    }

    @Given("a client with added hook")
    public void aClientWithAddedHook() {
        MockHook hook = new MockHook();
        state.hook = hook;
        state.client.addHooks(hook);
    }

    @Then("the {string} hook should have been executed")
    public void theHookShouldHaveBeenExecuted(String hookName) {
        assertHookCalled(hookName);
    }

    public void assertHookCalled(String hookName) {
        if ("before".equals(hookName)) {
            assertTrue(state.hook.isBeforeCalled());
        } else if ("after".equals(hookName)) {
            assertTrue(state.hook.isAfterCalled());
        } else if ("error".equals(hookName)) {
            assertTrue(state.hook.isErrorCalled());
        } else if ("finally".equals(hookName)) {
            assertTrue(state.hook.isFinallyAfterCalled());
        } else {
            throw new IllegalArgumentException(hookName + " is not a valid hook name");
        }
    }

    @And("the {string} hooks should be called with evaluation details")
    public void theHooksShouldBeCalledWithEvaluationDetails(String hookNames, DataTable data) {
        for (String hookName : hookNames.split(", ")) {
            assertHookCalled(hookName);
            FlagEvaluationDetails evaluationDetails =
                    state.hook.getEvaluationDetails().get(hookName);
            assertNotNull(evaluationDetails);
            List<Map<String, String>> dataEntries = data.asMaps();
            for (Map<String, String> line : dataEntries) {
                String key = line.get("key");
                Object expected = Utils.convert(line.get("value"), line.get("data_type"));
                Object actual;
                if ("flag_key".equals(key)) {
                    actual = evaluationDetails.getFlagKey();
                } else if ("value".equals(key)) {
                    actual = evaluationDetails.getValue();
                } else if ("variant".equals(key)) {
                    actual = evaluationDetails.getVariant();
                } else if ("reason".equals(key)) {
                    actual = evaluationDetails.getReason();
                } else if ("error_code".equals(key)) {
                    actual = evaluationDetails.getErrorCode();
                    if (actual != null) {
                        actual = actual.toString();
                    }
                } else {
                    throw new IllegalArgumentException(key + " is not a valid key");
                }

                assertEquals(expected, actual);
            }
        }
    }
}

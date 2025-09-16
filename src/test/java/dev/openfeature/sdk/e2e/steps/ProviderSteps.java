package dev.openfeature.sdk.e2e.steps;

import static dev.openfeature.sdk.testutils.TestFlagsUtils.buildFlags;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.ProviderEventDetails;
import dev.openfeature.sdk.ProviderState;
import dev.openfeature.sdk.Reason;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.e2e.State;
import dev.openfeature.sdk.exceptions.FatalError;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.Map;
import org.awaitility.Awaitility;

public class ProviderSteps {
    private final State state;

    public ProviderSteps(State state) {
        this.state = state;
    }

    @Given("a {} provider")
    public void a_provider_with_status(String providerType) throws Exception {
        // Normalize input to handle both single word and quoted strings
        String normalizedType =
                providerType.toLowerCase().replaceAll("[\"\\s]+", " ").trim();

        switch (normalizedType) {
            case "not ready":
                setupMockProvider(ErrorCode.PROVIDER_NOT_READY, "Provider in not ready state", ProviderState.NOT_READY);
                break;
            case "stable":
            case "ready":
                setupStableProvider();
                break;
            case "fatal":
                setupMockProvider(ErrorCode.PROVIDER_FATAL, "Provider in fatal state", ProviderState.FATAL);
                break;
            case "error":
                setupMockProvider(ErrorCode.GENERAL, "Provider in error state", ProviderState.ERROR);
                break;
            case "stale":
                setupMockProvider(null, null, ProviderState.STALE);
                break;
            default:
                throw new IllegalArgumentException("Unsupported provider type: " + providerType);
        }
    }

    // ===============================
    // Provider Status Assertion Steps
    // ===============================

    @Then("the provider status should be {string}")
    public void the_provider_status_should_be(String expectedStatus) {
        ProviderState actualStatus = state.client.getProviderState();
        ProviderState expected = ProviderState.valueOf(expectedStatus);
        assertThat(actualStatus).isEqualTo(expected);
    }

    // ===============================
    // Helper Methods
    // ===============================

    private void setupStableProvider() throws Exception {
        Map<String, Flag<?>> flags = buildFlags();
        InMemoryProvider provider = new InMemoryProvider(flags);
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        state.client = OpenFeatureAPI.getInstance().getClient();
    }

    private void setupMockProvider(ErrorCode errorCode, String errorMessage, ProviderState providerState)
            throws Exception {
        EventProvider mockProvider = spy(EventProvider.class);

        switch (providerState) {
            case NOT_READY:
                doAnswer(invocationOnMock -> {
                            while (true) {}
                        })
                        .when(mockProvider)
                        .initialize(any());
                break;
            case FATAL:
                doThrow(new FatalError(errorMessage)).when(mockProvider).initialize(any());
                break;
        }
        // Configure all evaluation methods with a single helper
        configureMockEvaluations(mockProvider, errorCode, errorMessage);

        OpenFeatureAPI.getInstance().setProvider(providerState.name(), mockProvider);
        Client client = OpenFeatureAPI.getInstance().getClient(providerState.name());
        state.client = client;

        ProviderEventDetails details =
                ProviderEventDetails.builder().errorCode(errorCode).build();
        switch (providerState) {
            case FATAL:
            case ERROR:
                mockProvider.emitProviderReady(details);
                mockProvider.emitProviderError(details);
                break;
            case STALE:
                mockProvider.emitProviderReady(details);
                mockProvider.emitProviderStale(details);
                break;
            default:
        }
        Awaitility.await().until(() -> {
            ProviderState providerState1 = client.getProviderState();
            return providerState1 == providerState;
        });
    }

    private void configureMockEvaluations(FeatureProvider mockProvider, ErrorCode errorCode, String errorMessage) {
        // Configure Boolean evaluation
        when(mockProvider.getBooleanEvaluation(anyString(), any(Boolean.class), any()))
                .thenAnswer(invocation -> createProviderEvaluation(invocation.getArgument(1), errorCode, errorMessage));

        // Configure String evaluation
        when(mockProvider.getStringEvaluation(anyString(), any(String.class), any()))
                .thenAnswer(invocation -> createProviderEvaluation(invocation.getArgument(1), errorCode, errorMessage));

        // Configure Integer evaluation
        when(mockProvider.getIntegerEvaluation(anyString(), any(Integer.class), any()))
                .thenAnswer(invocation -> createProviderEvaluation(invocation.getArgument(1), errorCode, errorMessage));

        // Configure Double evaluation
        when(mockProvider.getDoubleEvaluation(anyString(), any(Double.class), any()))
                .thenAnswer(invocation -> createProviderEvaluation(invocation.getArgument(1), errorCode, errorMessage));

        // Configure Object evaluation
        when(mockProvider.getObjectEvaluation(anyString(), any(Value.class), any()))
                .thenAnswer(invocation -> createProviderEvaluation(invocation.getArgument(1), errorCode, errorMessage));
    }

    private <T> ProviderEvaluation<T> createProviderEvaluation(
            T defaultValue, ErrorCode errorCode, String errorMessage) {
        return ProviderEvaluation.<T>builder()
                .value(defaultValue)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .reason(Reason.ERROR.toString())
                .build();
    }
}

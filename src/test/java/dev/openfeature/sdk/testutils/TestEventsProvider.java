package dev.openfeature.sdk.testutils;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.ProviderEvent;
import dev.openfeature.sdk.ProviderEventDetails;
import dev.openfeature.sdk.ProviderState;
import dev.openfeature.sdk.Value;

public class TestEventsProvider extends EventProvider {

    private boolean initError = false;
    private String initErrorMessage;
    private ProviderState state = ProviderState.NOT_READY;
    private boolean shutDown = false;
    private int initTimeout = 0;

    @Override
    public ProviderState getState() {
        return this.state;
    }

    public TestEventsProvider(int initTimeout) {
        this.initTimeout = initTimeout;
    }

    public TestEventsProvider(boolean initError, String initErrorMessage) {
        this.initError = initError;
        this.initErrorMessage = initErrorMessage;
    }

    public TestEventsProvider(ProviderState initialState) {
        this.state = initialState;
    }

    public void mockEvent(ProviderEvent event, ProviderEventDetails details) {
        emit(event, details);
    }

    public boolean isShutDown() {
        return this.shutDown;
    }

    @Override
    public void shutdown() {
        this.shutDown = true;
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        if (ProviderState.NOT_READY.equals(state)) {
            // wait half the TIMEOUT, otherwise some init/errors can be fired before we add handlers
            Thread.sleep(initTimeout);
            if (this.initError) {
                this.state = ProviderState.ERROR;
                throw new Exception(initErrorMessage);
            }
            this.state = ProviderState.READY;
        }
    }

    @Override
    public Metadata getMetadata() {
        throw new UnsupportedOperationException("Unimplemented method 'getMetadata'");
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue,
            EvaluationContext ctx) {
        throw new UnsupportedOperationException("Unimplemented method 'getBooleanEvaluation'");
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue,
            EvaluationContext ctx) {
        throw new UnsupportedOperationException("Unimplemented method 'getStringEvaluation'");
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue,
            EvaluationContext ctx) {
        throw new UnsupportedOperationException("Unimplemented method 'getIntegerEvaluation'");
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue,
            EvaluationContext ctx) {
        throw new UnsupportedOperationException("Unimplemented method 'getDoubleEvaluation'");
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue,
            EvaluationContext ctx) {
        throw new UnsupportedOperationException("Unimplemented method 'getObjectEvaluation'");
    }
};
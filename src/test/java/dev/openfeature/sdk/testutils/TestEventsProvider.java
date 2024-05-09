package dev.openfeature.sdk.testutils;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.ProviderEvent;
import dev.openfeature.sdk.ProviderEventDetails;
import dev.openfeature.sdk.ProviderState;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.GeneralError;

public class TestEventsProvider extends EventProvider {

    private boolean initError = false;
    private String initErrorMessage;
    private ProviderState state = ProviderState.NOT_READY;
    private boolean shutDown = false;
    private int initTimeoutMs = 0;
    private String name = "test";
    private Metadata metadata = new Metadata() {
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDomain() {
            return name;
        }
    };

    @Override
    public ProviderState getState() {
        return this.state;
    }

    public TestEventsProvider(int initTimeoutMs) {
        this.initTimeoutMs = initTimeoutMs;
    }

    public TestEventsProvider(int initTimeoutMs, boolean initError, String initErrorMessage) {
        this.initTimeoutMs = initTimeoutMs;
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
            Thread.sleep(initTimeoutMs);
            if (this.initError) {
                this.state = ProviderState.ERROR;
                throw new GeneralError(initErrorMessage);
            }
            this.state = ProviderState.READY;
        }
    }

    @Override
    public Metadata getMetadata() {
        return this.metadata;
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
package dev.openfeature.sdk.testutils;

import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.Metadata;
import dev.openfeature.api.ProviderEvaluation;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderEventDetails;
import dev.openfeature.api.Reason;
import dev.openfeature.api.Value;
import dev.openfeature.api.exceptions.FatalError;
import dev.openfeature.api.exceptions.GeneralError;
import dev.openfeature.sdk.EventProvider;

public class TestEventsProvider extends EventProvider {
    public static final String PASSED_IN_DEFAULT = "Passed in default";

    private boolean initError = false;
    private String initErrorMessage;
    private boolean shutDown = false;
    private int initTimeoutMs = 0;
    private String name = "test";
    private Metadata metadata = () -> name;
    private boolean isFatalInitError = false;

    public TestEventsProvider() {}

    public TestEventsProvider(int initTimeoutMs) {
        this.initTimeoutMs = initTimeoutMs;
    }

    public TestEventsProvider(int initTimeoutMs, boolean initError, String initErrorMessage) {
        this.initTimeoutMs = initTimeoutMs;
        this.initError = initError;
        this.initErrorMessage = initErrorMessage;
    }

    public TestEventsProvider(int initTimeoutMs, boolean initError, String initErrorMessage, boolean fatal) {
        this.initTimeoutMs = initTimeoutMs;
        this.initError = initError;
        this.initErrorMessage = initErrorMessage;
        this.isFatalInitError = fatal;
    }

    public static TestEventsProvider newInitializedTestEventsProvider() throws Exception {
        TestEventsProvider provider = new TestEventsProvider();
        provider.initialize(null);
        return provider;
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
        // wait half the TIMEOUT, otherwise some init/errors can be fired before we add handlers
        Thread.sleep(initTimeoutMs);
        if (this.initError) {
            if (this.isFatalInitError) {
                throw new FatalError(initErrorMessage);
            }
            throw new GeneralError(initErrorMessage);
        }
    }

    @Override
    public Metadata getMetadata() {
        return this.metadata;
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Boolean>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(
            String key, Value defaultValue, EvaluationContext invocationContext) {
        return ProviderEvaluation.<Value>builder()
                .value(defaultValue)
                .variant(PASSED_IN_DEFAULT)
                .reason(Reason.DEFAULT.toString())
                .build();
    }
}

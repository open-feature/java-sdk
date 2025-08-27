package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.Metadata;
import dev.openfeature.api.ProviderEvaluation;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderEventDetails;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.Value;
import dev.openfeature.api.exceptions.FatalError;
import dev.openfeature.api.exceptions.GeneralError;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeatureProviderStateManagerTest {

    private FeatureProviderStateManager wrapper;
    private TestDelegate testDelegate;

    @BeforeEach
    public void setUp() {
        testDelegate = new TestDelegate();
        wrapper = new FeatureProviderStateManager(testDelegate);
    }

    @Test
    void shouldOnlyCallInitOnce() throws Exception {
        wrapper.initialize(null);
        wrapper.initialize(null);
        assertThat(testDelegate.initCalled.get()).isOne();
    }

    @Test
    void shouldCallInitTwiceWhenShutDownInTheMeantime() throws Exception {
        wrapper.initialize(null);
        wrapper.shutdown();
        wrapper.initialize(null);
        assertThat(testDelegate.initCalled.get()).isEqualTo(2);
        assertThat(testDelegate.shutdownCalled.get()).isOne();
    }

    @Test
    void shouldSetStateToNotReadyAfterConstruction() {
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
    }

    @Test
    @Specification(
            number = "1.7.3",
            text =
                    "The client's provider status accessor MUST indicate READY if the initialize function of the associated provider terminates normally.")
    void shouldSetStateToReadyAfterInit() throws Exception {
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        wrapper.initialize(null);
        assertThat(wrapper.getState()).isEqualTo(ProviderState.READY);
    }

    @Test
    void shouldSetStateToNotReadyAfterShutdown() throws Exception {
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        wrapper.initialize(null);
        assertThat(wrapper.getState()).isEqualTo(ProviderState.READY);
        wrapper.shutdown();
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
    }

    @Specification(
            number = "1.7.4",
            text =
                    "The client's provider status accessor MUST indicate ERROR if the initialize function of the associated provider terminates abnormally.")
    @Test
    void shouldSetStateToErrorAfterErrorOnInit() {
        testDelegate.throwOnInit = new Exception();
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        assertThrows(Exception.class, () -> wrapper.initialize(null));
        assertThat(wrapper.getState()).isEqualTo(ProviderState.ERROR);
    }

    @Specification(
            number = "1.7.4",
            text =
                    "The client's provider status accessor MUST indicate ERROR if the initialize function of the associated provider terminates abnormally.")
    @Test
    void shouldSetStateToErrorAfterOpenFeatureErrorOnInit() {
        testDelegate.throwOnInit = new GeneralError();
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        assertThrows(GeneralError.class, () -> wrapper.initialize(null));
        assertThat(wrapper.getState()).isEqualTo(ProviderState.ERROR);
    }

    @Specification(
            number = "1.7.5",
            text =
                    "The client's provider status accessor MUST indicate FATAL if the initialize function of the associated provider terminates abnormally and indicates error code PROVIDER_FATAL.")
    @Test
    void shouldSetStateToErrorAfterFatalErrorOnInit() {
        testDelegate.throwOnInit = new FatalError();
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        assertThrows(FatalError.class, () -> wrapper.initialize(null));
        assertThat(wrapper.getState()).isEqualTo(ProviderState.FATAL);
    }

    @Specification(
            number = "5.3.5",
            text =
                    "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @Test
    void shouldSetTheStateToReadyWhenAReadyEventIsEmitted() {
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        wrapper.onEmit(ProviderEvent.PROVIDER_READY, null);
        assertThat(wrapper.getState()).isEqualTo(ProviderState.READY);
    }

    @Specification(
            number = "5.3.5",
            text =
                    "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @Test
    void shouldSetTheStateToStaleWhenAStaleEventIsEmitted() {
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        wrapper.onEmit(ProviderEvent.PROVIDER_STALE, null);
        assertThat(wrapper.getState()).isEqualTo(ProviderState.STALE);
    }

    @Specification(
            number = "5.3.5",
            text =
                    "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @Test
    void shouldSetTheStateToErrorWhenAnErrorEventIsEmitted() {
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        wrapper.onEmit(
                ProviderEvent.PROVIDER_ERROR,
                ProviderEventDetails.builder().errorCode(ErrorCode.GENERAL).build());
        assertThat(wrapper.getState()).isEqualTo(ProviderState.ERROR);
    }

    @Specification(
            number = "5.3.5",
            text =
                    "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @Test
    void shouldSetTheStateToFatalWhenAFatalErrorEventIsEmitted() {
        assertThat(wrapper.getState()).isEqualTo(ProviderState.NOT_READY);
        wrapper.onEmit(
                ProviderEvent.PROVIDER_ERROR,
                ProviderEventDetails.builder()
                        .errorCode(ErrorCode.PROVIDER_FATAL)
                        .build());
        assertThat(wrapper.getState()).isEqualTo(ProviderState.FATAL);
    }

    static class TestDelegate extends EventProvider {
        private final AtomicInteger initCalled = new AtomicInteger();
        private final AtomicInteger shutdownCalled = new AtomicInteger();
        private @Nullable Exception throwOnInit;

        @Override
        public Metadata getMetadata() {
            return null;
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext ctx) {
            return null;
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            return null;
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext ctx) {
            return null;
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            return null;
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            return null;
        }

        @Override
        public void initialize(EvaluationContext evaluationContext) throws Exception {
            initCalled.incrementAndGet();
            if (throwOnInit != null) {
                throw throwOnInit;
            }
        }

        @Override
        public void shutdown() {
            shutdownCalled.incrementAndGet();
        }
    }
}

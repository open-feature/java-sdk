package dev.openfeature.sdk;

import dev.openfeature.api.AbstractEventProvider;
import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.Provider;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.events.ProviderEventDetails;
import dev.openfeature.api.exceptions.OpenFeatureError;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FeatureProviderStateManager implements EventProviderListener {
    private static final Logger log = LoggerFactory.getLogger(FeatureProviderStateManager.class);
    private final Provider delegate;
    private final AtomicBoolean isInitialized = new AtomicBoolean();
    private final AtomicReference<ProviderState> state = new AtomicReference<>(ProviderState.NOT_READY);

    public FeatureProviderStateManager(Provider delegate) {
        this.delegate = delegate;
        if (delegate instanceof AbstractEventProvider) {
            ((AbstractEventProvider) delegate)
                    .setEventEmitter(new EventEmitter((AbstractEventProvider) delegate, this));
        }
    }

    public void initialize(EvaluationContext evaluationContext) throws Exception {
        if (isInitialized.getAndSet(true)) {
            return;
        }
        try {
            delegate.initialize(evaluationContext);
            setState(ProviderState.READY);
        } catch (OpenFeatureError openFeatureError) {
            if (ErrorCode.PROVIDER_FATAL.equals(openFeatureError.getErrorCode())) {
                setState(ProviderState.FATAL);
            } else {
                setState(ProviderState.ERROR);
            }
            isInitialized.set(false);
            throw openFeatureError;
        } catch (Exception e) {
            setState(ProviderState.ERROR);
            isInitialized.set(false);
            throw e;
        }
    }

    public void shutdown() {
        delegate.shutdown();
        setState(ProviderState.NOT_READY);
        isInitialized.set(false);
    }

    @Override
    public void onEmit(ProviderEvent event, ProviderEventDetails details) {
        if (ProviderEvent.PROVIDER_ERROR.equals(event)) {
            if (details != null && details.getErrorCode() == ErrorCode.PROVIDER_FATAL) {
                setState(ProviderState.FATAL);
            } else {
                setState(ProviderState.ERROR);
            }
        } else if (ProviderEvent.PROVIDER_STALE.equals(event)) {
            setState(ProviderState.STALE);
        } else if (ProviderEvent.PROVIDER_READY.equals(event)) {
            setState(ProviderState.READY);
        }
    }

    private void setState(ProviderState state) {
        ProviderState oldState = this.state.getAndSet(state);
        if (oldState != state) {
            String providerName;
            if (delegate.getMetadata() == null || delegate.getMetadata().getName() == null) {
                providerName = "unknown";
            } else {
                providerName = delegate.getMetadata().getName();
            }
            log.info("Provider {} transitioned from state {} to state {}", providerName, oldState, state);
        }
    }

    public ProviderState getState() {
        return state.get();
    }

    Provider getProvider() {
        return delegate;
    }

    public boolean hasSameProvider(Provider featureProvider) {
        return this.delegate.equals(featureProvider);
    }
}

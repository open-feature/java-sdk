package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.OpenFeatureError;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;

class FeatureProviderStateManager implements EventProviderListener {
    private final FeatureProvider delegate;
    private final AtomicBoolean isInitialized = new AtomicBoolean();

    @Getter
    private ProviderState state = ProviderState.NOT_READY;

    public FeatureProviderStateManager(FeatureProvider delegate) {
        this.delegate = delegate;
        if (delegate instanceof EventProvider) {
            ((EventProvider) delegate).setEventProviderListener(this);
        }
    }

    public void initialize(EvaluationContext evaluationContext) throws Exception {
        if (isInitialized.getAndSet(true)) {
            return;
        }
        try {
            delegate.initialize(evaluationContext);
            state = ProviderState.READY;
        } catch (OpenFeatureError openFeatureError) {
            if (ErrorCode.PROVIDER_FATAL.equals(openFeatureError.getErrorCode())) {
                state = ProviderState.FATAL;
            } else {
                state = ProviderState.ERROR;
            }
            isInitialized.set(false);
            throw openFeatureError;
        } catch (Exception e) {
            state = ProviderState.ERROR;
            isInitialized.set(false);
            throw e;
        }
    }

    public void shutdown() {
        delegate.shutdown();
        state = ProviderState.NOT_READY;
        isInitialized.set(false);
    }

    @Override
    public void onEmit(ProviderEvent event, ProviderEventDetails details) {
        if (ProviderEvent.PROVIDER_ERROR.equals(event)) {
            if (details != null && details.getErrorCode() == ErrorCode.PROVIDER_FATAL) {
                state = ProviderState.FATAL;
            } else {
                state = ProviderState.ERROR;
            }
        } else if (ProviderEvent.PROVIDER_STALE.equals(event)) {
            state = ProviderState.STALE;
        } else if (ProviderEvent.PROVIDER_READY.equals(event)) {
            state = ProviderState.READY;
        }
    }

    FeatureProvider getProvider() {
        return delegate;
    }

    public boolean hasSameProvider(FeatureProvider featureProvider) {
        return this.delegate.equals(featureProvider);
    }
}

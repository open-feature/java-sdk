package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.OpenFeatureError;
import lombok.experimental.Delegate;

import java.util.concurrent.atomic.AtomicBoolean;

class StatefulFeatureProvider implements FeatureProvider, EventProviderListener {

    private interface ExcludeFromDelegate {
        void initialize(EvaluationContext evaluationContext) throws Exception;

        void shutdown();

        ProviderState getState();
    }

    @Delegate(excludes = ExcludeFromDelegate.class)
    private final FeatureProvider delegate;
    private final AtomicBoolean isInitialized = new AtomicBoolean();
    private ProviderState state = ProviderState.NOT_READY;

    public StatefulFeatureProvider(FeatureProvider delegate) {
        this.delegate = delegate;
        if (delegate instanceof EventProvider) {
            ((EventProvider) delegate).setEventProviderListener(this);
        }
    }

    @Override
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

    @Override
    public void shutdown() {
        delegate.shutdown();
        state = ProviderState.NOT_READY;
        isInitialized.set(false);
    }

    @Override
    public ProviderState getState() {
        return state;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StatefulFeatureProvider) {
            return delegate.equals(((StatefulFeatureProvider) obj).delegate);
        }
        return delegate.equals(obj);
    }

    @Override
    public void onEmit(ProviderEvent event, ProviderEventDetails details) {
        if (ProviderEvent.PROVIDER_ERROR.equals(event)) {
            state = ProviderState.ERROR;
        } else if (ProviderEvent.PROVIDER_STALE.equals(event)) {
            state = ProviderState.STALE;
        } else if (ProviderEvent.PROVIDER_READY.equals(event)) {
            state = ProviderState.READY;
        }
    }

    FeatureProvider getDelegate() {
        return delegate;
    }
}

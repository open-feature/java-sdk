package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.OpenFeatureError;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FeatureProviderWrapper implements FeatureProvider, EventProviderListener {
    private final FeatureProvider delegate;
    private final AtomicBoolean isInitialized = new AtomicBoolean();
    private ProviderState state = ProviderState.NOT_READY;

    public FeatureProviderWrapper(FeatureProvider delegate) {
        this.delegate = delegate;
        if (delegate instanceof EventProvider) {
            ((EventProvider) delegate).setEventProviderListener(this);
        }
    }

    @Override
    public Metadata getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public List<Hook> getProviderHooks() {
        return delegate.getProviderHooks();
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return delegate.getBooleanEvaluation(key, defaultValue, ctx);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return delegate.getStringEvaluation(key, defaultValue, ctx);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return delegate.getIntegerEvaluation(key, defaultValue, ctx);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return delegate.getDoubleEvaluation(key, defaultValue, ctx);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        return delegate.getObjectEvaluation(key, defaultValue, ctx);
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
        if (this == obj) return true;
        if (obj instanceof FeatureProviderWrapper) {
            return delegate.equals(((FeatureProviderWrapper) obj).delegate);
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

    FeatureProvider getDelegate(){
        return delegate;
    }
}

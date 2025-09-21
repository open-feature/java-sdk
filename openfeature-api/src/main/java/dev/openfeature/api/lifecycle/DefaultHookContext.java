package dev.openfeature.api.lifecycle;

import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.types.ClientMetadata;
import dev.openfeature.api.types.ProviderMetadata;

/**
 * A default implementation of {@link HookContext}.
 */
final class DefaultHookContext<T> implements HookContext<T> {

    private final String flagKey;
    private final T defaultValue;
    private final FlagValueType type;
    private final ProviderMetadata providerMetadata;
    private final ClientMetadata clientMetadata;
    private final EvaluationContext evaluationContext;
    private final HookData hookData = new DefaultHookData();

    DefaultHookContext(
            String flagKey,
            T defaultValue,
            FlagValueType type,
            ProviderMetadata providerMetadata,
            ClientMetadata clientMetadata,
            EvaluationContext evaluationContext) {
        this.flagKey = flagKey;
        this.defaultValue = defaultValue;
        this.type = type;
        this.providerMetadata = providerMetadata;
        this.clientMetadata = clientMetadata;
        this.evaluationContext = evaluationContext;
    }

    @Override
    public String getFlagKey() {
        return flagKey;
    }

    @Override
    public FlagValueType getType() {
        return type;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public EvaluationContext getCtx() {
        return evaluationContext;
    }

    @Override
    public ClientMetadata getClientMetadata() {
        return clientMetadata;
    }

    @Override
    public ProviderMetadata getProviderMetadata() {
        return providerMetadata;
    }

    @Override
    public HookData getHookData() {
        return hookData;
    }
}

package dev.openfeature.sdk;

import lombok.Getter;

@Getter
class SharedHookContext<T> {

    private final String key;
    private final FlagValueType type;
    private final ClientMetadata clientMetadata;
    private final Metadata providerMetadata;
    private final T defaultValue;

    public SharedHookContext(
            String key, FlagValueType type, ClientMetadata clientMetadata, Metadata providerMetadata, T defaultValue) {
        this.key = key;
        this.type = type;
        this.clientMetadata = clientMetadata;
        this.providerMetadata = providerMetadata;
        this.defaultValue = defaultValue;
    }

    public HookContext<T> hookContextFor(EvaluationContext evaluationContext, HookData hookData) {
        return new HookContext<>(this, evaluationContext, hookData);
    }
}

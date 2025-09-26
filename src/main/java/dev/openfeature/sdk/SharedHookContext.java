package dev.openfeature.sdk;

import java.util.Objects;
import lombok.Getter;

@Getter
class SharedHookContext<T> {

    private final String flagKey;
    private final FlagValueType type;
    private final ClientMetadata clientMetadata;
    private final Metadata providerMetadata;
    private final T defaultValue;

    public SharedHookContext(
            String flagKey,
            FlagValueType type,
            ClientMetadata clientMetadata,
            Metadata providerMetadata,
            T defaultValue) {
        this.flagKey = flagKey;
        this.type = type;
        this.clientMetadata = clientMetadata;
        this.providerMetadata = providerMetadata;
        this.defaultValue = defaultValue;
    }

    public HookContext<T> hookContextFor(EvaluationContext evaluationContext, HookData hookData) {
        return new HookContext<>(this, evaluationContext, hookData);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SharedHookContext<?> that = (SharedHookContext<?>) o;
        return Objects.equals(flagKey, that.flagKey)
                && type == that.type
                && Objects.equals(clientMetadata, that.clientMetadata)
                && Objects.equals(providerMetadata, that.providerMetadata)
                && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flagKey, type, clientMetadata, providerMetadata, defaultValue);
    }
}

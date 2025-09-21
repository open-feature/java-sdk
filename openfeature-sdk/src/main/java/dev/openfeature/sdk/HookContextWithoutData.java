package dev.openfeature.sdk;

import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Hook;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.lifecycle.HookContext;
import dev.openfeature.api.types.ClientMetadata;
import dev.openfeature.api.types.ProviderMetadata;

/**
 * A data class to hold immutable context that {@link Hook} instances use.
 *
 * @param <T> the type for the flag being evaluated
 */
final class HookContextWithoutData<T> implements HookContext<T> {
    private final String flagKey;

    private final FlagValueType type;

    private final T defaultValue;

    private EvaluationContext ctx;

    private final ClientMetadata clientMetadata;
    private final ProviderMetadata providerMetadata;

    HookContextWithoutData(
            String flagKey,
            FlagValueType type,
            T defaultValue,
            ClientMetadata clientMetadata,
            ProviderMetadata providerMetadata,
            EvaluationContext ctx) {
        if (flagKey == null) {
            throw new NullPointerException("flagKey is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue is null");
        }
        if (ctx == null) {
            throw new NullPointerException("ctx is null");
        }
        this.type = type;
        this.flagKey = flagKey;
        this.ctx = ctx;
        this.defaultValue = defaultValue;
        this.clientMetadata = clientMetadata;
        this.providerMetadata = providerMetadata;
    }

    /**
     * Builds a {@link HookContextWithoutData} instances from request data.
     *
     * @param key              feature flag key
     * @param type             flag value type
     * @param clientMetadata   info on which client is calling
     * @param providerMetadata info on the provider
     * @param defaultValue     Fallback value
     * @param <T>              type that the flag is evaluating against
     * @return resulting context for hook
     */
    static <T> HookContextWithoutData<T> of(
            String key,
            FlagValueType type,
            ClientMetadata clientMetadata,
            ProviderMetadata providerMetadata,
            T defaultValue) {
        return new HookContextWithoutData<>(
                key, type, defaultValue, clientMetadata, providerMetadata, EvaluationContext.EMPTY);
    }

    public static <T> HookContext<T> of(String flagKey, FlagValueType flagValueType, T defaultValue) {
        return new HookContextWithoutData<>(flagKey, flagValueType, defaultValue, null, null, EvaluationContext.EMPTY);
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
        return ctx;
    }

    void setCtx(EvaluationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public ClientMetadata getClientMetadata() {
        return clientMetadata;
    }

    @Override
    public ProviderMetadata getProviderMetadata() {
        return providerMetadata;
    }
}

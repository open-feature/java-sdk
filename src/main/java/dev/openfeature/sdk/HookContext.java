package dev.openfeature.sdk;

import lombok.NonNull;

/**
 * A data class to hold immutable context that {@link Hook} instances use.
 *
 * @param <T> the type for the flag being evaluated
 */
public final class HookContext<T> {
    @NonNull
    private final String flagKey;

    @NonNull
    private final FlagValueType type;

    @NonNull
    private final T defaultValue;

    @NonNull
    private EvaluationContext ctx;

    private final ClientMetadata clientMetadata;
    private final Metadata providerMetadata;

    private final HookData hookData;

    HookContext(@NonNull String flagKey, @NonNull FlagValueType type, @NonNull T defaultValue,
            @NonNull EvaluationContext ctx, ClientMetadata clientMetadata, Metadata providerMetadata,
            HookData hookData) {
        this.flagKey = flagKey;
        this.type = type;
        this.defaultValue = defaultValue;
        this.ctx = ctx;
        this.clientMetadata = clientMetadata;
        this.providerMetadata = providerMetadata;
        this.hookData = hookData;
    }

    /**
     * Builds {@link HookContext} instances from request data.
     *
     * @param key              feature flag key
     * @param type             flag value type
     * @param clientMetadata   info on which client is calling
     * @param providerMetadata info on the provider
     * @param ctx              Evaluation Context for the request
     * @param defaultValue     Fallback value
     * @param <T>              type that the flag is evaluating against
     * @return resulting context for hook
     */
    public static <T> HookContext<T> from(
            String key,
            FlagValueType type,
            ClientMetadata clientMetadata,
            Metadata providerMetadata,
            EvaluationContext ctx,
            T defaultValue) {
        return HookContext.<T>builder()
                .flagKey(key)
                .type(type)
                .clientMetadata(clientMetadata)
                .providerMetadata(providerMetadata)
                .ctx(ctx)
                .defaultValue(defaultValue)
                .hookData(null)
                .build();
    }

    public static <T> HookContextBuilder<T> builder() {return new HookContextBuilder<T>();}

    public @NonNull String getFlagKey() {
        return this.flagKey;
    }

    public @NonNull FlagValueType getType() {
        return this.type;
    }

    public @NonNull T getDefaultValue() {
        return this.defaultValue;
    }

    public @NonNull EvaluationContext getCtx() {
        return this.ctx;
    }

    public ClientMetadata getClientMetadata() {
        return this.clientMetadata;
    }

    public Metadata getProviderMetadata() {
        return this.providerMetadata;
    }

    public HookData getHookData() {
        return this.hookData;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HookContext)) {
            return false;
        }
        final HookContext<?> other = (HookContext<?>) o;
        final Object this$flagKey = this.getFlagKey();
        final Object other$flagKey = other.getFlagKey();
        if (this$flagKey == null ? other$flagKey != null : !this$flagKey.equals(other$flagKey)) {
            return false;
        }
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) {
            return false;
        }
        final Object this$defaultValue = this.getDefaultValue();
        final Object other$defaultValue = other.getDefaultValue();
        if (this$defaultValue == null ? other$defaultValue != null : !this$defaultValue.equals(other$defaultValue)) {
            return false;
        }
        final Object this$ctx = this.getCtx();
        final Object other$ctx = other.getCtx();
        if (this$ctx == null ? other$ctx != null : !this$ctx.equals(other$ctx)) {
            return false;
        }
        final Object this$clientMetadata = this.getClientMetadata();
        final Object other$clientMetadata = other.getClientMetadata();
        if (this$clientMetadata == null
                ? other$clientMetadata != null
                : !this$clientMetadata.equals(other$clientMetadata)) {
            return false;
        }
        final Object this$providerMetadata = this.getProviderMetadata();
        final Object other$providerMetadata = other.getProviderMetadata();
        if (this$providerMetadata == null
                ? other$providerMetadata != null
                : !this$providerMetadata.equals(other$providerMetadata)) {
            return false;
        }
        final Object this$hookData = this.getHookData();
        final Object other$hookData = other.getHookData();
        if (this$hookData == null ? other$hookData != null : !this$hookData.equals(other$hookData)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $flagKey = this.getFlagKey();
        result = result * PRIME + ($flagKey == null ? 43 : $flagKey.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $defaultValue = this.getDefaultValue();
        result = result * PRIME + ($defaultValue == null ? 43 : $defaultValue.hashCode());
        final Object $ctx = this.getCtx();
        result = result * PRIME + ($ctx == null ? 43 : $ctx.hashCode());
        final Object $clientMetadata = this.getClientMetadata();
        result = result * PRIME + ($clientMetadata == null ? 43 : $clientMetadata.hashCode());
        final Object $providerMetadata = this.getProviderMetadata();
        result = result * PRIME + ($providerMetadata == null ? 43 : $providerMetadata.hashCode());
        final Object $hookData = this.getHookData();
        result = result * PRIME + ($hookData == null ? 43 : $hookData.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "HookContext(flagKey=" + this.getFlagKey() + ", type=" + this.getType() + ", defaultValue="
                + this.getDefaultValue() + ", ctx=" + this.getCtx() + ", clientMetadata=" + this.getClientMetadata()
                + ", providerMetadata=" + this.getProviderMetadata() + ", hookData=" + this.getHookData() + ")";
    }

    void setCtx(@NonNull EvaluationContext ctx) {
        this.ctx = ctx;
    }

    public HookContext<T> withFlagKey(@NonNull String flagKey) {
        return this.flagKey == flagKey ? this
                : new HookContext<T>(flagKey, this.type, this.defaultValue, this.ctx, this.clientMetadata,
                        this.providerMetadata, this.hookData);
    }

    public HookContext<T> withType(@NonNull FlagValueType type) {
        return this.type == type ? this
                : new HookContext<T>(this.flagKey, type, this.defaultValue, this.ctx, this.clientMetadata,
                        this.providerMetadata, this.hookData);
    }

    public HookContext<T> withDefaultValue(@NonNull T defaultValue) {
        return this.defaultValue == defaultValue ? this
                : new HookContext<T>(this.flagKey, this.type, defaultValue, this.ctx, this.clientMetadata,
                        this.providerMetadata, this.hookData);
    }

    public HookContext<T> withCtx(@NonNull EvaluationContext ctx) {
        return this.ctx == ctx ? this
                : new HookContext<T>(this.flagKey, this.type, this.defaultValue, ctx, this.clientMetadata,
                        this.providerMetadata, this.hookData);
    }

    public HookContext<T> withClientMetadata(ClientMetadata clientMetadata) {
        return this.clientMetadata == clientMetadata ? this
                : new HookContext<T>(this.flagKey, this.type, this.defaultValue, this.ctx, clientMetadata,
                        this.providerMetadata, this.hookData);
    }

    public HookContext<T> withProviderMetadata(Metadata providerMetadata) {
        return this.providerMetadata == providerMetadata ? this
                : new HookContext<T>(this.flagKey, this.type, this.defaultValue, this.ctx, this.clientMetadata,
                        providerMetadata, this.hookData);
    }

    public HookContext<T> withHookData(HookData hookData) {
        return this.hookData == hookData ? this
                : new HookContext<T>(this.flagKey, this.type, this.defaultValue, this.ctx, this.clientMetadata,
                        this.providerMetadata, hookData);
    }

    public static class HookContextBuilder<T> {
        private @NonNull String flagKey;
        private @NonNull FlagValueType type;
        private @NonNull T defaultValue;
        private @NonNull EvaluationContext ctx;
        private ClientMetadata clientMetadata;
        private Metadata providerMetadata;
        private HookData hookData;

        HookContextBuilder() {}

        public HookContextBuilder<T> flagKey(@NonNull String flagKey) {
            this.flagKey = flagKey;
            return this;
        }

        public HookContextBuilder<T> type(@NonNull FlagValueType type) {
            this.type = type;
            return this;
        }

        public HookContextBuilder<T> defaultValue(@NonNull T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public HookContextBuilder<T> ctx(@NonNull EvaluationContext ctx) {
            this.ctx = ctx;
            return this;
        }

        public HookContextBuilder<T> clientMetadata(ClientMetadata clientMetadata) {
            this.clientMetadata = clientMetadata;
            return this;
        }

        public HookContextBuilder<T> providerMetadata(Metadata providerMetadata) {
            this.providerMetadata = providerMetadata;
            return this;
        }

        public HookContextBuilder<T> hookData(HookData hookData) {
            this.hookData = hookData;
            return this;
        }

        public HookContext<T> build() {
            return new HookContext<T>(this.flagKey, this.type, this.defaultValue, this.ctx, this.clientMetadata,
                    this.providerMetadata, this.hookData);
        }

        public String toString() {
            return "HookContext.HookContextBuilder(flagKey=" + this.flagKey + ", type=" + this.type + ", defaultValue="
                    + this.defaultValue + ", ctx=" + this.ctx + ", clientMetadata=" + this.clientMetadata
                    + ", providerMetadata=" + this.providerMetadata + ", hookData=" + this.hookData + ")";
        }
    }
}

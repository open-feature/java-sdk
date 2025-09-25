package dev.openfeature.sdk;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import lombok.Generated;
import lombok.NonNull;

/**
 * A data class to hold immutable context that {@link Hook} instances use.
 *
 * @param <T> the type for the flag being evaluated
 */
public final class HookContext<T> {
    private final SharedHookContext<T> sharedContext;
    private EvaluationContext ctx;
    private final HookData hookData;

    HookContext(@NonNull SharedHookContext<T> sharedContext, EvaluationContext evaluationContext, HookData hookData) {
        this.sharedContext = sharedContext;
        ctx = evaluationContext;
        this.hookData = hookData;
    }

    /**
     * Obsolete constructor.
     * This constructor is retained for binary compatibility but is no longer part of the public API.
     *
     * @param flagKey          feature flag key
     * @param type             flag value type
     * @param clientMetadata   info on which client is calling
     * @param providerMetadata info on the provider
     * @param ctx              Evaluation Context for the request
     * @param defaultValue     Fallback value
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Deprecated
    HookContext(
            @NonNull String flagKey,
            @NonNull FlagValueType type,
            @NonNull T defaultValue,
            @NonNull EvaluationContext ctx,
            ClientMetadata clientMetadata,
            Metadata providerMetadata,
            HookData hookData) {
        this(new SharedHookContext<>(flagKey, type, clientMetadata, providerMetadata, defaultValue), ctx, hookData);
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
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Deprecated
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

    /**
     * Creates a new builder for {@link HookContext}.
     *
     * @param <T> the type for the flag being evaluated
     * @return a new builder
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Deprecated
    public static <T> HookContextBuilder<T> builder() {
        return new HookContextBuilder<T>();
    }

    public @NonNull String getFlagKey() {
        return sharedContext.getFlagKey();
    }

    @Generated
    public @NonNull FlagValueType getType() {
        return sharedContext.getType();
    }

    public @NonNull T getDefaultValue() {
        return sharedContext.getDefaultValue();
    }

    public @NonNull EvaluationContext getCtx() {
        return this.ctx;
    }

    public ClientMetadata getClientMetadata() {
        return sharedContext.getClientMetadata();
    }

    public Metadata getProviderMetadata() {
        return sharedContext.getProviderMetadata();
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Intentional exposure of hookData")
    public HookData getHookData() {
        return this.hookData;
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HookContext<?> that = (HookContext<?>) o;
        return Objects.equals(ctx, that.ctx)
                && Objects.equals(hookData, that.hookData)
                && Objects.equals(sharedContext, that.sharedContext);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(ctx, hookData, sharedContext);
    }

    @Generated
    @Override
    public String toString() {
        return "HookContext(flagKey=" + this.getFlagKey() + ", type=" + this.getType() + ", defaultValue="
                + this.getDefaultValue() + ", ctx=" + this.getCtx() + ", clientMetadata=" + this.getClientMetadata()
                + ", providerMetadata=" + this.getProviderMetadata() + ", hookData=" + this.getHookData() + ")";
    }

    void setCtx(@NonNull EvaluationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Returns a new HookContext with the provided flagKey if it is different from the current one.
     *
     * @param flagKey new flag key
     * @return new HookContext with updated flagKey or the same instance if unchanged
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public HookContext<T> withFlagKey(@NonNull String flagKey) {
        return Objects.equals(this.getFlagKey(), flagKey)
                ? this
                : new HookContext<T>(
                        flagKey,
                        this.getType(),
                        this.getDefaultValue(),
                        this.getCtx(),
                        this.getClientMetadata(),
                        this.getProviderMetadata(),
                        this.hookData);
    }

    /**
     * Returns a new HookContext with the provided type if it is different from the current one.
     *
     * @param type new flag value type
     * @return new HookContext with updated type or the same instance if unchanged
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public HookContext<T> withType(@NonNull FlagValueType type) {
        return this.getType() == type
                ? this
                : new HookContext<T>(
                        this.getFlagKey(),
                        type,
                        this.getDefaultValue(),
                        this.getCtx(),
                        this.getClientMetadata(),
                        this.getProviderMetadata(),
                        this.hookData);
    }

    /**
     * Returns a new HookContext with the provided defaultValue if it is different from the current one.
     *
     * @param defaultValue new default value
     * @return new HookContext with updated defaultValue or the same instance if unchanged
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public HookContext<T> withDefaultValue(@NonNull T defaultValue) {
        return this.getDefaultValue() == defaultValue
                ? this
                : new HookContext<T>(
                        this.getFlagKey(),
                        this.getType(),
                        defaultValue,
                        this.getCtx(),
                        this.getClientMetadata(),
                        this.getProviderMetadata(),
                        this.hookData);
    }

    /**
     * Returns a new HookContext with the provided ctx if it is different from the current one.
     *
     * @param ctx new evaluation context
     * @return new HookContext with updated ctx or the same instance if unchanged
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public HookContext<T> withCtx(@NonNull EvaluationContext ctx) {
        return this.ctx == ctx
                ? this
                : new HookContext<T>(
                        this.getFlagKey(),
                        this.getType(),
                        this.getDefaultValue(),
                        ctx,
                        this.getClientMetadata(),
                        this.getProviderMetadata(),
                        this.hookData);
    }

    /**
     * Returns a new HookContext with the provided clientMetadata if it is different from the current one.
     *
     * @param clientMetadata new client metadata
     * @return new HookContext with updated clientMetadata or the same instance if unchanged
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public HookContext<T> withClientMetadata(ClientMetadata clientMetadata) {
        return this.getClientMetadata() == clientMetadata
                ? this
                : new HookContext<T>(
                        this.getFlagKey(),
                        this.getType(),
                        this.getDefaultValue(),
                        this.getCtx(),
                        clientMetadata,
                        this.getProviderMetadata(),
                        this.hookData);
    }

    /**
     * Returns a new HookContext with the provided providerMetadata if it is different from the current one.
     *
     * @param providerMetadata new provider metadata
     * @return new HookContext with updated providerMetadata or the same instance if unchanged
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public HookContext<T> withProviderMetadata(Metadata providerMetadata) {
        return this.getProviderMetadata() == providerMetadata
                ? this
                : new HookContext<T>(
                        this.getFlagKey(),
                        this.getType(),
                        this.getDefaultValue(),
                        this.getCtx(),
                        this.getClientMetadata(),
                        providerMetadata,
                        this.hookData);
    }

    /**
     * Returns a new HookContext with the provided hookData if it is different from the current one.
     *
     * @param hookData new hook data
     * @return new HookContext with updated hookData or the same instance if unchanged
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public HookContext<T> withHookData(HookData hookData) {
        return this.hookData == hookData
                ? this
                : new HookContext<T>(
                        this.getFlagKey(),
                        this.getType(),
                        this.getDefaultValue(),
                        this.getCtx(),
                        this.getClientMetadata(),
                        this.getProviderMetadata(),
                        hookData);
    }

    /**
     * Builder for HookContext.
     *
     * @param <T> The flag type.
     * @deprecated HookContext is initialized by the SDK and passed to hooks. Users should not create new instances.
     */
    @Generated
    @Deprecated
    public static class HookContextBuilder<T> {
        private String flagKey;
        private FlagValueType type;
        private T defaultValue;
        private EvaluationContext ctx;
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

        @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Intentional exposure of hookData")
        public HookContextBuilder<T> hookData(HookData hookData) {
            this.hookData = hookData;
            return this;
        }

        /**
         * Builds the HookContext instance.
         *
         * @return a new HookContext
         */
        public HookContext<T> build() {
            return new HookContext<T>(
                    this.flagKey,
                    this.type,
                    this.defaultValue,
                    this.ctx,
                    this.clientMetadata,
                    this.providerMetadata,
                    this.hookData);
        }

        @Generated
        @Override
        public String toString() {
            return "HookContext.HookContextBuilder(flagKey=" + this.flagKey + ", type=" + this.type + ", defaultValue="
                    + this.defaultValue + ", ctx=" + this.ctx + ", clientMetadata=" + this.clientMetadata
                    + ", providerMetadata=" + this.providerMetadata + ", hookData=" + this.hookData + ")";
        }
    }
}

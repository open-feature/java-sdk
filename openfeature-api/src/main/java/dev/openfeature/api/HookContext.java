package dev.openfeature.api;

import java.util.Objects;

/**
 * A data class to hold immutable context that {@link Hook} instances use.
 *
 * @param <T> the type for the flag being evaluated
 */
public final class HookContext<T> {
    private final String flagKey;
    private final FlagValueType type;
    private final T defaultValue;
    private final EvaluationContext ctx;
    private final ClientMetadata clientMetadata;
    private final Metadata providerMetadata;

    private HookContext(Builder<T> builder) {
        this.flagKey = Objects.requireNonNull(builder.flagKey, "flagKey cannot be null");
        this.type = Objects.requireNonNull(builder.type, "type cannot be null");
        this.defaultValue = Objects.requireNonNull(builder.defaultValue, "defaultValue cannot be null");
        this.ctx = Objects.requireNonNull(builder.ctx, "ctx cannot be null");
        this.clientMetadata = builder.clientMetadata;
        this.providerMetadata = builder.providerMetadata;
    }

    public String getFlagKey() {
        return flagKey;
    }

    public FlagValueType getType() {
        return type;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public EvaluationContext getCtx() {
        return ctx;
    }

    public ClientMetadata getClientMetadata() {
        return clientMetadata;
    }

    public Metadata getProviderMetadata() {
        return providerMetadata;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HookContext<?> that = (HookContext<?>) o;
        return Objects.equals(flagKey, that.flagKey)
                && type == that.type
                && Objects.equals(defaultValue, that.defaultValue)
                && Objects.equals(ctx, that.ctx)
                && Objects.equals(clientMetadata, that.clientMetadata)
                && Objects.equals(providerMetadata, that.providerMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flagKey, type, defaultValue, ctx, clientMetadata, providerMetadata);
    }

    @Override
    public String toString() {
        return "HookContext{"
                + "flagKey='" + flagKey + '\''
                + ", type=" + type
                + ", defaultValue=" + defaultValue
                + ", ctx=" + ctx
                + ", clientMetadata=" + clientMetadata
                + ", providerMetadata=" + providerMetadata
                + '}';
    }

    /**
     * Builder for HookContext instances.
     *
     * @param <T> the type for the flag being evaluated
     */
    public static final class Builder<T> {
        private String flagKey;
        private FlagValueType type;
        private T defaultValue;
        private EvaluationContext ctx;
        private ClientMetadata clientMetadata;
        private Metadata providerMetadata;

        private Builder() {}

        public Builder<T> flagKey(String flagKey) {
            this.flagKey = flagKey;
            return this;
        }

        public Builder<T> type(FlagValueType type) {
            this.type = type;
            return this;
        }

        public Builder<T> defaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<T> ctx(EvaluationContext ctx) {
            this.ctx = ctx;
            return this;
        }

        public Builder<T> clientMetadata(ClientMetadata clientMetadata) {
            this.clientMetadata = clientMetadata;
            return this;
        }

        public Builder<T> providerMetadata(Metadata providerMetadata) {
            this.providerMetadata = providerMetadata;
            return this;
        }

        public HookContext<T> build() {
            return new HookContext<>(this);
        }
    }
}

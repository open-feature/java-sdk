package dev.openfeature.sdk.providers.memory;

import dev.openfeature.api.ImmutableMetadata;
import java.util.Map;
import java.util.Objects;

/**
 * Flag representation for the in-memory provider.
 */
public class Flag<T> {
    private final Map<String, Object> variants;
    private final String defaultVariant;
    private final ContextEvaluator<T> contextEvaluator;
    private final ImmutableMetadata flagMetadata;

    private Flag(Builder<T> builder) {
        this.variants = builder.variants;
        this.defaultVariant = builder.defaultVariant;
        this.contextEvaluator = builder.contextEvaluator;
        this.flagMetadata = builder.flagMetadata;
    }

    public Map<String, Object> getVariants() {
        return variants;
    }

    public String getDefaultVariant() {
        return defaultVariant;
    }

    public ContextEvaluator<T> getContextEvaluator() {
        return contextEvaluator;
    }

    public ImmutableMetadata getFlagMetadata() {
        return flagMetadata;
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
        Flag<?> flag = (Flag<?>) o;
        return Objects.equals(variants, flag.variants)
                && Objects.equals(defaultVariant, flag.defaultVariant)
                && Objects.equals(contextEvaluator, flag.contextEvaluator)
                && Objects.equals(flagMetadata, flag.flagMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variants, defaultVariant, contextEvaluator, flagMetadata);
    }

    @Override
    public String toString() {
        return "Flag{" + "variants="
                + variants + ", defaultVariant='"
                + defaultVariant + '\'' + ", contextEvaluator="
                + contextEvaluator + ", flagMetadata="
                + flagMetadata + '}';
    }

    /**
     * Builder class for Flag.
     *
     * @param <T> the flag type
     */
    public static class Builder<T> {
        private Map<String, Object> variants = new java.util.HashMap<>();
        private String defaultVariant;
        private ContextEvaluator<T> contextEvaluator;
        private ImmutableMetadata flagMetadata;

        public Builder<T> variants(Map<String, Object> variants) {
            this.variants = variants;
            return this;
        }

        public Builder<T> variant(String key, Object value) {
            this.variants.put(key, value);
            return this;
        }

        public Builder<T> defaultVariant(String defaultVariant) {
            this.defaultVariant = defaultVariant;
            return this;
        }

        public Builder<T> contextEvaluator(ContextEvaluator<T> contextEvaluator) {
            this.contextEvaluator = contextEvaluator;
            return this;
        }

        public Builder<T> flagMetadata(ImmutableMetadata flagMetadata) {
            this.flagMetadata = flagMetadata;
            return this;
        }

        public Flag<T> build() {
            return new Flag<>(this);
        }
    }
}

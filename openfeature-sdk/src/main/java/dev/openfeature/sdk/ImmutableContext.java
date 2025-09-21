package dev.openfeature.sdk;

import dev.openfeature.api.evaluation.ImmutableContext as ApiImmutableContext;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.types.Structure;
import dev.openfeature.api.types.Value;
import java.util.Map;

/**
 * @deprecated Use {@link dev.openfeature.api.evaluation.ImmutableContext} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.ImmutableContext;
 * ImmutableContext context = ImmutableContext.builder()
 *     .targetingKey("user123")
 *     .add("age", 25)
 *     .build();
 *
 * // After
 * import dev.openfeature.api.evaluation.ImmutableContext;
 * ImmutableContext context = ImmutableContext.builder()
 *     .targetingKey("user123")
 *     .add("age", 25)
 *     .build();
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@SuppressWarnings("deprecation")
public final class ImmutableContext extends ApiImmutableContext {

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.ImmutableContext#builder()} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    private ImmutableContext(String targetingKey, Map<String, Value> attributes) {
        super(targetingKey, attributes);
    }

    /**
     * Builder pattern for backward compatibility.
     * @deprecated Use {@link dev.openfeature.api.evaluation.ImmutableContext#builder()} directly.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ImmutableContextBuilder builder() {
        return new ImmutableContextBuilder();
    }

    /**
     * Create an ImmutableContext with targeting key.
     * @deprecated Use {@link dev.openfeature.api.evaluation.ImmutableContext#of(String)} directly.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ImmutableContext of(String targetingKey) {
        ApiImmutableContext apiContext = ApiImmutableContext.of(targetingKey);
        return fromApiContext(apiContext);
    }

    /**
     * Create an ImmutableContext with targeting key and attributes.
     * @deprecated Use {@link dev.openfeature.api.evaluation.ImmutableContext#of(String, Map)} directly.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ImmutableContext of(String targetingKey, Map<String, Value> attributes) {
        ApiImmutableContext apiContext = ApiImmutableContext.of(targetingKey, attributes);
        return fromApiContext(apiContext);
    }

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.ImmutableContext.Builder} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final class ImmutableContextBuilder {
        private final ApiImmutableContext.Builder apiBuilder = ApiImmutableContext.builder();

        public ImmutableContextBuilder targetingKey(String targetingKey) {
            apiBuilder.targetingKey(targetingKey);
            return this;
        }

        public ImmutableContextBuilder add(String key, Value value) {
            apiBuilder.add(key, value);
            return this;
        }

        public ImmutableContextBuilder add(String key, String value) {
            apiBuilder.add(key, value);
            return this;
        }

        public ImmutableContextBuilder add(String key, Boolean value) {
            apiBuilder.add(key, value);
            return this;
        }

        public ImmutableContextBuilder add(String key, Integer value) {
            apiBuilder.add(key, value);
            return this;
        }

        public ImmutableContextBuilder add(String key, Double value) {
            apiBuilder.add(key, value);
            return this;
        }

        public ImmutableContextBuilder add(String key, Structure value) {
            apiBuilder.add(key, value);
            return this;
        }

        public ImmutableContextBuilder addAll(EvaluationContext context) {
            apiBuilder.addAll(context);
            return this;
        }

        public ImmutableContext build() {
            ApiImmutableContext apiContext = apiBuilder.build();
            return fromApiContext(apiContext);
        }
    }

    private static ImmutableContext fromApiContext(ApiImmutableContext apiContext) {
        return new ImmutableContext(apiContext.getTargetingKey(), apiContext.asMap());
    }
}
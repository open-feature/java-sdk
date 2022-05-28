package dev.openfeature.javasdk;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;

public interface Hook<T> {
    default Optional<EvaluationContext> before(HookContext<T> ctx, ImmutableMap<String, Object> hints) {
        return Optional.empty();
    }
    default void after(HookContext<T> ctx, FlagEvaluationDetails<T> details, ImmutableMap<String, Object> hints) {}
    default void error(HookContext<T> ctx, Exception error, ImmutableMap<String, Object> hints) {}
    default void finallyAfter(HookContext<T> ctx, ImmutableMap<String, Object> hints) {}
}

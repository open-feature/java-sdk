package dev.openfeature.javasdk;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;

// TODO: interface? or abstract class?
public abstract class Hook<T> {
    public Optional<EvaluationContext> before(HookContext<T> ctx, ImmutableMap<String, Object> hints) {
        return null;
    }
    public void after(HookContext<T> ctx, FlagEvaluationDetails<T> details, ImmutableMap<String, Object> hints) {}
    public void error(HookContext<T> ctx, Exception error, ImmutableMap<String, Object> hints) {}
    public void finallyAfter(HookContext<T> ctx, ImmutableMap<String, Object> hints) {}
}

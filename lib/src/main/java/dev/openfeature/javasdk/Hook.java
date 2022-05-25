package dev.openfeature.javasdk;

import com.google.common.collect.ImmutableMap;

// TODO: interface? or abstract class?
// I suppose an interface with no-op default methods is slightly more flexible, since you can implement multiple interfaces?
public abstract class Hook<T> {
    public void before(HookContext<T> ctx, ImmutableMap<String, Object> hints) {}
    public void after(HookContext<T> ctx, FlagEvaluationDetails<T> details, ImmutableMap<String, Object> hints) {}
    public void error(HookContext<T> ctx, Exception error, ImmutableMap<String, Object> hints) {}
    public void finallyAfter(HookContext<T> ctx, ImmutableMap<String, Object> hints) {}
}

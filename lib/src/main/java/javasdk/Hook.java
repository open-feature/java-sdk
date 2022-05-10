package javasdk;

import com.google.common.collect.ImmutableMap;

// TODO: interface? or abstract class?
public abstract class Hook<T> {
    void before(HookContext<T> ctx, ImmutableMap<String, Object> hints) {}
    void after(HookContext<T> ctx, FlagEvaluationDetails<T> details, ImmutableMap<String, Object> hints) {}
    void error(HookContext<T> ctx, Exception error, ImmutableMap<String, Object> hints) {}
    void finallyAfter(HookContext<T> ctx, ImmutableMap<String, Object> hints) {}
}

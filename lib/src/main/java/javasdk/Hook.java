package javasdk;

// TODO: interface? or abstract class?
public abstract class Hook<T> {
    void before(HookContext<T> ctx) {}
    void after(HookContext<T> ctx, FlagEvaluationDetails<T> details) {}
    void error(HookContext<T> ctx, Exception error) {}
    void finallyAfter(HookContext<T> ctx) {}
}

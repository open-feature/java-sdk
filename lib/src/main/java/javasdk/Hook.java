package javasdk;

public interface Hook<T> {
    void before(HookContext<T> ctx);
    void after(HookContext<T> ctx, FlagEvaluationDetails<T> details);
    void error(HookContext<T> ctx, Exception error);
    void afterAll(HookContext<T> ctx);
}

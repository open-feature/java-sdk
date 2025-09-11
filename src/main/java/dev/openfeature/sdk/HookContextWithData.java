package dev.openfeature.sdk;

class HookContextWithData<T> implements HookContext<T> {
    private final HookContext<T> context;
    private final HookData data;

    private HookContextWithData(HookContext<T> context, HookData data) {
        this.context = context;
        this.data = data;
    }

    public static <T> HookContextWithData<T> of(HookContext<T> context, HookData data) {
        return new HookContextWithData<>(context, data);
    }

    @Override
    public String getFlagKey() {
        return context.getFlagKey();
    }

    @Override
    public FlagValueType getType() {
        return context.getType();
    }

    @Override
    public T getDefaultValue() {
        return context.getDefaultValue();
    }

    @Override
    public EvaluationContext getCtx() {
        return context.getCtx();
    }

    @Override
    public ClientMetadata getClientMetadata() {
        return context.getClientMetadata();
    }

    @Override
    public Metadata getProviderMetadata() {
        return context.getProviderMetadata();
    }

    @Override
    public HookData getHookData() {
        return data;
    }
}

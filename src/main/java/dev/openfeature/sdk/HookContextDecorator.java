package dev.openfeature.sdk;

/**
 * A base decorator class for {@link HookContext} that enables dynamic enhancement of its functionality.
 * This class wraps an existing {@code HookContext<T>} instance and delegates method calls to it.
 *
 * @param <T> the type for the flag being evaluated
 */
class HookContextDecorator<T> extends HookContext<T> {

    HookContext<T> decorated;

    protected HookContextDecorator(HookContext<T> context) {
        super(context.getFlagKey(), context.getType(), context.getDefaultValue(), context.getCtx(),
                context.getClientMetadata(), context.getProviderMetadata());
        this.decorated = context;
    }

    @Override
    public HookData getHookData() {
        return decorated.getHookData();
    }
}

package dev.openfeature.sdk;

/**
 * A concrete decorator for {@link HookContext} that adds {@link HookData} to the existing functionality.
 *
 * @param <T> the type for the flag being evaluated
 */
public class HookContextWithData<T> extends HookContextDecorator<T> {

    private final HookData hookData;

    public HookContextWithData(HookContext<T> context, HookData data) {
        super(context);
        this.hookData = data;
    }

    @Override
    public HookData getHookData() {
        return hookData;
    }
}

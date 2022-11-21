package dev.openfeature.sdk;

/**
 * {@inheritDoc}
 */
public interface StringHook extends Hook<String> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.STRING == flagValueType;
    }
}

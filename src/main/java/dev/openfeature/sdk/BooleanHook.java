package dev.openfeature.sdk;

/**
 * {@inheritDoc}
 */
public interface BooleanHook extends Hook<Boolean> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.BOOLEAN == flagValueType;
    }
}

package dev.openfeature.sdk;

/**
 * {@inheritDoc}
 */
public interface IntegerHook extends Hook<Integer> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.INTEGER == flagValueType;
    }
}

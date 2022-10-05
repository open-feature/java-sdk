package dev.openfeature.sdk;

public interface IntegerHook extends Hook<Integer> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.INTEGER == flagValueType;
    }
}

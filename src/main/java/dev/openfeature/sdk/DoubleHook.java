package dev.openfeature.sdk;

public interface DoubleHook extends Hook<Double> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.DOUBLE == flagValueType;
    }
}
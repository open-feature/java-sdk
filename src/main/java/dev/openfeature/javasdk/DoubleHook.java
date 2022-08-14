package dev.openfeature.javasdk;

public interface DoubleHook extends Hook<Double> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.DOUBLE == flagValueType;
    }
}
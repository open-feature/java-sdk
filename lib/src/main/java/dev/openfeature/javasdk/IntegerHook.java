package dev.openfeature.javasdk;

public interface IntegerHook extends Hook<Integer> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.INTEGER == flagValueType;
    }
}

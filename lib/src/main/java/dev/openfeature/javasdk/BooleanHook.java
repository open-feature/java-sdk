package dev.openfeature.javasdk;

public interface BooleanHook extends Hook<Boolean> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.BOOLEAN == flagValueType;
    }
}

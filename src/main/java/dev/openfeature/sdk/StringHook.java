package dev.openfeature.sdk;

public interface StringHook extends Hook<String> {

    @Override
    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return FlagValueType.STRING == flagValueType;
    }
}

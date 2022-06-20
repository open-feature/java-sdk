package dev.openfeature.javasdk;

public interface StringHook extends Hook<String> {

    @Override
    default FlagValueType supportsFlagValueType() {
        return FlagValueType.STRING;
    }
}

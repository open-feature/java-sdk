package dev.openfeature.javasdk;

public interface BooleanHook extends Hook<Boolean> {

    @Override
    default FlagValueType supportsFlagValueType() {
        return FlagValueType.BOOLEAN;
    }
}

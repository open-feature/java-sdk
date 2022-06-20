package dev.openfeature.javasdk;

public interface IntegerHook extends Hook<Integer> {

    @Override
    default FlagValueType supportsFlagValueType() {
        return FlagValueType.INTEGER;
    }
}

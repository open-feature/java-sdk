package dev.openfeature.api.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Hook;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HookTest {

    @ParameterizedTest
    @MethodSource("provideKeyValuePairs")
    void supportsFlagValueType(Hook<?> hook, FlagValueType flagValueType) {
        for (FlagValueType value : FlagValueType.values()) {
            assertThat(hook.supportsFlagValueType(value)).isEqualTo(flagValueType == value);
        }
    }

    static Stream<Arguments> provideKeyValuePairs() {
        return Stream.of(
                Arguments.of(new BooleanHook() {}, FlagValueType.BOOLEAN),
                Arguments.of(new StringHook() {}, FlagValueType.STRING),
                Arguments.of(new DoubleHook() {}, FlagValueType.DOUBLE),
                Arguments.of(new IntegerHook() {}, FlagValueType.INTEGER));
    }
}

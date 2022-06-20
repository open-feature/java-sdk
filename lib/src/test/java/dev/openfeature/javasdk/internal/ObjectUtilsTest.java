package dev.openfeature.javasdk.internal;

import java.util.*;

import org.junit.jupiter.api.*;

import static dev.openfeature.javasdk.internal.ObjectUtils.defaultIfNull;
import static org.assertj.core.api.Assertions.assertThat;

class ObjectUtilsTest {

    @Nested
    class GenericObject {
        @Test
        @DisplayName("should return default value if null")
        void shouldReturnDefaultValueIfNull() {
            var defaultValue = "default";

            var actual = defaultIfNull(null, () -> defaultValue);

            assertThat(actual).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should return given value if not null")
        void shouldReturnGivenValueIfNotNull() {
            var defaultValue = "default";
            var expectedValue = "expected";

            var actual = defaultIfNull(expectedValue, () -> defaultValue);

            assertThat(actual).isEqualTo(expectedValue);
        }
    }

    @Nested
    class ListSupport {

        @Test
        @DisplayName("should return default list if given one is null")
        void shouldReturnDefaultListIfGivenOneIsNull() {
            var defaultValue = List.of("default");

            var actual = defaultIfNull(null, () -> defaultValue);

            assertThat(actual).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should return given list if not null")
        void shouldReturnGivenListIfNotNull() {
            var defaultValue = List.of("default");
            var expectedValue = List.of("expected");

            var actual = defaultIfNull(expectedValue, () -> defaultValue);

            assertThat(actual).isEqualTo(expectedValue);
        }
    }

    @Nested
    class MapSupport {

        @Test
        @DisplayName("should return default map if given one is null")
        void shouldReturnDefaultMapIfGivenOneIsNull() {
            var defaultValue = Map.of("key", "default");

            var actual = defaultIfNull(null, () -> defaultValue);

            assertThat(actual).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should return given map if not null")
        void shouldReturnGivenMapIfNotNull() {
            var defaultValue = Map.of("key", "default");
            var expectedValue = Map.of("key", "expected");

            var actual = defaultIfNull(expectedValue, () -> defaultValue);

            assertThat(actual).isEqualTo(expectedValue);
        }
    }


}

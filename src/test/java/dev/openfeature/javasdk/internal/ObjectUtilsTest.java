package dev.openfeature.javasdk.internal;

import java.util.*;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.*;

import static dev.openfeature.javasdk.internal.ObjectUtils.defaultIfNull;
import static org.assertj.core.api.Assertions.assertThat;

class ObjectUtilsTest {

    @Nested
    class GenericObject {
        @Test
        @DisplayName("should return default value if null")
        void shouldReturnDefaultValueIfNull() {
            String defaultValue = "default";

            String actual = defaultIfNull(null, () -> defaultValue);

            assertThat(actual).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should return given value if not null")
        void shouldReturnGivenValueIfNotNull() {
            String defaultValue = "default";
            String expectedValue = "expected";

            String actual = defaultIfNull(expectedValue, () -> defaultValue);

            assertThat(actual).isEqualTo(expectedValue);
        }
    }

    @Nested
    class ListSupport {

        @Test
        @DisplayName("should return default list if given one is null")
        void shouldReturnDefaultListIfGivenOneIsNull() {
            List<String> defaultValue = Collections.singletonList("default");

            List<String> actual = defaultIfNull(null, () -> defaultValue);

            assertThat(actual).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should return given list if not null")
        void shouldReturnGivenListIfNotNull() {
            List<String> defaultValue = Collections.singletonList("default");
            List<String> expectedValue = Collections.singletonList("expected");

            List<String> actual = defaultIfNull(expectedValue, () -> defaultValue);

            assertThat(actual).isEqualTo(expectedValue);
        }
    }

    @Nested
    class MapSupport {

        @Test
        @DisplayName("should return default map if given one is null")
        void shouldReturnDefaultMapIfGivenOneIsNull() {
            Map<String, Object> defaultValue = ImmutableMap.of("key", "default");

            Map<String, Object> actual = defaultIfNull(null, () -> defaultValue);

            assertThat(actual).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should return given map if not null")
        void shouldReturnGivenMapIfNotNull() {
            Map<String, String> defaultValue = ImmutableMap.of("key", "default");
            Map<String, String> expectedValue = ImmutableMap.of("key", "expected");

            Map<String, String> actual = defaultIfNull(expectedValue, () -> defaultValue);

            assertThat(actual).isEqualTo(expectedValue);
        }
    }


}

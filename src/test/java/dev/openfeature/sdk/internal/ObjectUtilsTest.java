package dev.openfeature.sdk.internal;

import static dev.openfeature.sdk.internal.ObjectUtils.defaultIfNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            HashMap<String, String> hm = new HashMap<>();
            hm.put("key", "default");
            Map<String, Object> defaultValue = Collections.unmodifiableMap(hm);

            Map<String, Object> actual = defaultIfNull(null, () -> defaultValue);

            assertThat(actual).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should return given map if not null")
        void shouldReturnGivenMapIfNotNull() {
            Map<String, String> dv = new HashMap<>();
            dv.put("key", "default");
            Map<String, String> defaultValue = Collections.unmodifiableMap(dv);

            Map<String, String> ev = new HashMap<>();
            ev.put("key", "expected");
            Map<String, String> expectedValue = Collections.unmodifiableMap(ev);

            Map<String, String> actual = defaultIfNull(expectedValue, () -> defaultValue);

            assertThat(actual).isEqualTo(expectedValue);
        }
    }
}

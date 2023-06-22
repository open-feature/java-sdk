package dev.openfeature.sdk.internal;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;

import static dev.openfeature.sdk.internal.ObjectUtils.defaultIfNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TriConsumerTest {

    @Test
    @DisplayName("should run accept")
    void shouldRunAccept() {
        AtomicInteger result = new AtomicInteger(0);
        TriConsumer<Integer, Integer, Integer> triConsumer = (num1, num2, num3) -> {
            result.set(result.get() + num1 + num2 + num3);
        };
        triConsumer.accept(1, 2, 3);
        assertEquals(6, result.get());
    }

    @Test
    @DisplayName("should run after accept")
    void shouldRunAfterAccept() {
        AtomicInteger result = new AtomicInteger(0);
        TriConsumer<Integer, Integer, Integer> triConsumer = (num1, num2, num3) -> {
            result.set(result.get() + num1 + num2 + num3);
        };
        TriConsumer composed = triConsumer.andThen(triConsumer);
        composed.accept(1, 2, 3);
        assertEquals(12, result.get());
    }
}
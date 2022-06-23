package dev.openfeature.javasdk.internal;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectUtils {

    public static <T> List<T> defaultIfNull(List<T> source, Supplier<List<T>> defaultValue) {
        if (source == null) {
            return defaultValue.get();
        }
        return source;
    }

    public static <K, V> Map<K, V> defaultIfNull(Map<K, V> source, Supplier<Map<K, V>> defaultValue) {
        if (source == null) {
            return defaultValue.get();
        }
        return source;
    }

    public static <T> T defaultIfNull(T source, Supplier<T> defaultValue) {
        if (source == null) {
            return defaultValue.get();
        }
        return source;
    }

    @SafeVarargs
    public static <T> List<T> merge(List<T>... sources) {
        return Arrays
            .stream(sources)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

}

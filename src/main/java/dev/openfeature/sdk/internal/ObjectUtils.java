package dev.openfeature.sdk.internal;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectUtils {

    /**
     * If the source param is null, return the default value.
     * @param source maybe null object
     * @param defaultValue thing to use if source is null
     * @param <T> list type
     * @return resulting object
     */
    public static <T> List<T> defaultIfNull(List<T> source, Supplier<List<T>> defaultValue) {
        if (source == null) {
            return defaultValue.get();
        }
        return source;
    }

    /**
     * If the source param is null, return the default value.
     * @param source maybe null object
     * @param defaultValue thing to use if source is null
     * @param <K> map key type
     * @param <V> map value type
     * @return resulting map
     */
    public static <K, V> Map<K, V> defaultIfNull(Map<K, V> source, Supplier<Map<K, V>> defaultValue) {
        if (source == null) {
            return defaultValue.get();
        }
        return source;
    }

    /**
     * If the source param is null, return the default value.
     * @param source maybe null object
     * @param defaultValue thing to use if source is null
     * @param <T> type
     * @return resulting object
     */
    public static <T> T defaultIfNull(T source, Supplier<T> defaultValue) {
        if (source == null) {
            return defaultValue.get();
        }
        return source;
    }

    /**
     * Concatenate a bunch of lists.
     * @param sources bunch of lists.
     * @param <T> list type
     * @return resulting object
     */
    @SafeVarargs
    public static <T> List<T> merge(List<T>... sources) {
        return Arrays
            .stream(sources)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

}

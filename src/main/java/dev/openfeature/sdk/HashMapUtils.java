package dev.openfeature.sdk;

import java.util.HashMap;

class HashMapUtils {
    private HashMapUtils() {}

    static <K, V> HashMap<K, V> forEntries(int expectedEntries) {
        return new HashMap<>((int) Math.ceil(expectedEntries / .75));
    }
}

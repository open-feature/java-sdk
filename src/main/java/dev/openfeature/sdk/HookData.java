package dev.openfeature.sdk;

/**
 * Hook data provides a way for hooks to maintain state across their execution stages.
 * Each hook instance gets its own isolated data store that persists only for the duration
 * of a single flag evaluation.
 */
public interface HookData {
    /**
     * Sets a value for the given key.
     *
     * @param key the key to store the value under
     * @param value the value to store
     */
    void set(String key, Object value);

    /**
     * Gets the value for the given key.
     *
     * @param key the key to retrieve the value for
     * @return the value, or null if not found
     */
    Object get(String key);

    /**
     * Gets the value for the given key, cast to the specified type.
     *
     * @param <T> the type to cast to
     * @param key the key to retrieve the value for
     * @param type the class to cast to
     * @return the value cast to the specified type, or null if not found
     * @throws ClassCastException if the value cannot be cast to the specified type
     */
    <T> T get(String key, Class<T> type);
}

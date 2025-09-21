package dev.openfeature.api.lifecycle;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of HookData.
 */
final class DefaultHookData implements HookData {
    Map<String, Object> data;

    @Override
    public void set(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    @Override
    public Object get(String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Value for key '" + key + "' is not of type " + type.getName());
        }
        return type.cast(value);
    }
}

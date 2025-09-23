package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of HookData.
 */
public class DefaultHookData implements HookData {
    private Map<String, Object> data;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        dev.openfeature.sdk.DefaultHookData that = (dev.openfeature.sdk.DefaultHookData) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }
}

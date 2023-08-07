package dev.openfeature.sdk.testutils;

import dev.openfeature.sdk.MutableStructure;
import dev.openfeature.sdk.Value;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Value utils.
 */
@UtilityClass
@Slf4j
public class ValueUtils {

    /**
     * Convert object to Value.
     * Supporting bean objects with field getters.
     * Note:
     * Not all objects may be supported.
     * @param object
     * @return
     */
    @SneakyThrows
    public static Value convert(Object object) {
        if (object == null) {
            return null;
        }
        if (ClassUtils.isPrimitiveOrWrapper(object.getClass()) || object instanceof String) {
            return new Value(object);
        }
        if (object instanceof Map) {
            Map<String, ?> map = (Map)object;
            Map<String, Value> values = new HashMap<>();
            map.entrySet().stream().forEach(entry -> {
                values.put(entry.getKey(), convert(entry.getValue()));
            });
            return new Value(new MutableStructure(values));
        }
        if (object instanceof List) {
            List<?> list = (List)object;
            return new Value(list.stream().map(p -> convert(p)).collect(Collectors.toList()));
        }
        Map<String, Object> map = convertObjectToMap(object);
        return convert(map);
    }

    private static Map<String, Object> convertObjectToMap(Object object) throws IllegalAccessException, InvocationTargetException {
        Map<String, Object> map = new HashMap<>();
        for (Field field: FieldUtils.getAllFields(object.getClass())) {
            try {
                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getterMethod = object.getClass().getMethod(getterName);
                Object value = getterMethod.invoke(object);
                map.put(field.getName(), value);
            } catch (NoSuchMethodException e) {
                log.debug("Skipping field: {}", field.getName());
            }

        }
        return map;
    }

}

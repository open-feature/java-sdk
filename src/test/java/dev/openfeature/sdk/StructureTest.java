package dev.openfeature.sdk;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.openfeature.sdk.Structure.mapToStructure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StructureTest {
    @Test
    public void noArgShouldContainEmptyAttributes() {
        MutableStructure structure = new MutableStructure();
        assertEquals(0, structure.asMap().keySet().size());
    }

    @Test
    public void mapArgShouldContainNewMap() {
        String KEY = "key";
        Map<String, Value> map = new HashMap<String, Value>() {
            {
                put(KEY, new Value(KEY));
            }
        };
        MutableStructure structure = new MutableStructure(map);
        assertEquals(KEY, structure.asMap().get(KEY).asString());
        assertNotSame(structure.asMap(), map); // should be a copy
    }

    @Test
    public void addAndGetAddAndReturnValues() {
        String BOOL_KEY = "bool";
        String STRING_KEY = "string";
        String INT_KEY = "int";
        String DOUBLE_KEY = "double";
        String DATE_KEY = "date";
        String STRUCT_KEY = "struct";
        String LIST_KEY = "list";
        String VALUE_KEY = "value";

        boolean BOOL_VAL = true;
        String STRING_VAL = "val";
        int INT_VAL = 13;
        double DOUBLE_VAL = .5;
        Instant DATE_VAL = Instant.now();
        MutableStructure STRUCT_VAL = new MutableStructure();
        List<Value> LIST_VAL = new ArrayList<>();
        Value VALUE_VAL = new Value();

        MutableStructure structure = new MutableStructure();
        structure.add(BOOL_KEY, BOOL_VAL);
        structure.add(STRING_KEY, STRING_VAL);
        structure.add(INT_KEY, INT_VAL);
        structure.add(DOUBLE_KEY, DOUBLE_VAL);
        structure.add(DATE_KEY, DATE_VAL);
        structure.add(STRUCT_KEY, STRUCT_VAL);
        structure.add(LIST_KEY, LIST_VAL);
        structure.add(VALUE_KEY, VALUE_VAL);

        assertEquals(BOOL_VAL, structure.getValue(BOOL_KEY).asBoolean());
        assertEquals(STRING_VAL, structure.getValue(STRING_KEY).asString());
        assertEquals(INT_VAL, structure.getValue(INT_KEY).asInteger());
        assertEquals(DOUBLE_VAL, structure.getValue(DOUBLE_KEY).asDouble());
        assertEquals(DATE_VAL, structure.getValue(DATE_KEY).asInstant());
        assertEquals(STRUCT_VAL, structure.getValue(STRUCT_KEY).asStructure());
        assertEquals(LIST_VAL, structure.getValue(LIST_KEY).asList());
        assertTrue(structure.getValue(VALUE_KEY).isNull());
    }

    @SneakyThrows
    @Test
    void mapToStructureTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("String", "str");
        map.put("Boolean", true);
        map.put("Integer", 1);
        map.put("Double", 1.1);
        map.put("List", Collections.singletonList(new Value(1)));
        map.put("Value", new Value((true)));
        map.put("Instant", Instant.ofEpochSecond(0));
        map.put("Map", new HashMap<>());
        map.put("nullKey", null);
        ImmutableContext immutableContext = new ImmutableContext();
        map.put("ImmutableContext", immutableContext);
        Structure res = mapToStructure(map);
        assertEquals(new Value("str"), res.getValue("String"));
        assertEquals(new Value(true), res.getValue("Boolean"));
        assertEquals(new Value(1), res.getValue("Integer"));
        assertEquals(new Value(1.1), res.getValue("Double"));
        assertEquals(new Value(Collections.singletonList(new Value(1))), res.getValue("List"));
        assertEquals(new Value(true), res.getValue("Value"));
        assertEquals(new Value(Instant.ofEpochSecond(0)), res.getValue("Instant"));
        assertEquals(new HashMap<>(), res.getValue("Map").asStructure().asMap());
        assertEquals(new Value(immutableContext), res.getValue("ImmutableContext"));
        assertEquals(new Value(), res.getValue("nullKey"));
    }

    @Test
    void asObjectHandlesNullValue() {
        Map<String, Value> map = new HashMap<>();
        map.put("null", new Value((String) null));
        ImmutableStructure structure = new ImmutableStructure(map);
        assertNull(structure.asObjectMap().get("null"));
    }

    @Test
    void convertValueHandlesNullValue() {
        ImmutableStructure structure = new ImmutableStructure();
        assertNull(structure.convertValue(new Value((String) null)));
    }
}

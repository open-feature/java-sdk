package dev.openfeature.javasdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class StructureTest {
    @Test public void noArgShouldContainEmptyAttributes() {
        Structure structure = new Structure();
        assertEquals(0, structure.asMap().keySet().size());
    }

    @Test public void mapArgShouldContainNewMap() {
        String KEY = "key";
        Map<String, Value> map = new HashMap<String, Value>() {
            {
                put(KEY, new Value(KEY));
            }
        };
        Structure structure = new Structure(map);
        assertEquals(KEY, structure.asMap().get(KEY).asString());
        assertNotSame(structure.asMap(), map); // should be a copy
    }

    @Test public void addAndGetAddAndReturnValues() {
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
        ZonedDateTime DATE_VAL = ZonedDateTime.now();
        Structure STRUCT_VAL = new Structure();
        List<Value> LIST_VAL = new ArrayList<Value>();
        Value VALUE_VAL = new Value();

        Structure structure = new Structure();
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
        assertEquals(DATE_VAL, structure.getValue(DATE_KEY).asZonedDateTime());
        assertEquals(STRUCT_VAL, structure.getValue(STRUCT_KEY).asStructure());
        assertEquals(LIST_VAL, structure.getValue(LIST_KEY).asList());
        assertTrue(structure.getValue(VALUE_KEY).isNull());
    }
}

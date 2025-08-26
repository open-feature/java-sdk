package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValueTest {
    @Test
    void noArgShouldContainNull() {
        Value value = new Value();
        assertTrue(value.isNull());
    }

    @Test
    void objectArgShouldContainObject() {
        try {
            // int is a special case, see intObjectArgShouldConvertToInt()
            List<Object> list = new ArrayList<>();
            list.add(true);
            list.add("val");
            list.add(.5);
            list.add(new MutableStructure());
            list.add(new ArrayList<Value>());
            list.add(Instant.now());

            int i = 0;
            for (Object l : list) {
                Value value = new Value(l);
                assertEquals(list.get(i), value.asObject());
                i++;
            }
        } catch (Exception e) {
            fail("No exception expected.");
        }
    }

    @Test
    void intObjectArgShouldConvertToInt() {
        try {
            Object innerValue = 1;
            Value value = new Value(innerValue);
            assertEquals(innerValue, value.asInteger());
        } catch (Exception e) {
            fail("No exception expected.");
        }
    }

    @Test
    void invalidObjectArgShouldThrow() {

        class Something {}

        assertThrows(InstantiationException.class, () -> {
            new Value(new Something());
        });
    }

    @Test
    void boolArgShouldContainBool() {
        boolean innerValue = true;
        Value value = new Value(innerValue);
        assertTrue(value.isBoolean());
        assertEquals(innerValue, value.asBoolean());
    }

    @Test
    void numericArgShouldReturnDoubleOrInt() {
        double innerDoubleValue = 1.75;
        Value doubleValue = new Value(innerDoubleValue);
        assertTrue(doubleValue.isNumber());
        assertEquals(1, doubleValue.asInteger()); // the double value represented by this object converted to type int
        assertEquals(1.75, doubleValue.asDouble());

        int innerIntValue = 100;
        Value intValue = new Value(innerIntValue);
        assertTrue(intValue.isNumber());
        assertEquals(innerIntValue, intValue.asInteger());
        assertEquals(innerIntValue, intValue.asDouble());
    }

    @Test
    void stringArgShouldContainString() {
        String innerValue = "hi!";
        Value value = new Value(innerValue);
        assertTrue(value.isString());
        assertEquals(innerValue, value.asString());
    }

    @Test
    void dateShouldContainDate() {
        Instant innerValue = Instant.now();
        Value value = new Value(innerValue);
        assertTrue(value.isInstant());
        assertEquals(innerValue, value.asInstant());
    }

    @Test
    void structureShouldContainStructure() {
        String INNER_KEY = "key";
        String INNER_VALUE = "val";
        MutableStructure innerValue = new MutableStructure().add(INNER_KEY, INNER_VALUE);
        Value value = new Value(innerValue);
        assertTrue(value.isStructure());
        assertEquals(INNER_VALUE, value.asStructure().getValue(INNER_KEY).asString());
    }

    @Test
    void listArgShouldContainList() {
        String ITEM_VALUE = "val";
        List<Value> innerValue = new ArrayList<Value>();
        innerValue.add(new Value(ITEM_VALUE));
        Value value = new Value(innerValue);
        assertTrue(value.isList());
        assertEquals(ITEM_VALUE, value.asList().get(0).asString());
    }

    @Test
    void listMustBeOfValues() {
        String item = "item";
        List<String> list = new ArrayList<>();
        list.add(item);
        try {
            new Value((Object) list);
            fail("Should fail due to creation of list of non-values.");
        } catch (InstantiationException e) {
            assertEquals("Invalid value type: class java.util.ArrayList", e.getMessage());
        }
    }

    @Test
    void emptyListAllowed() {
        List<String> list = new ArrayList<>();
        try {
            Value value = new Value((Object) list);
            assertTrue(value.isList());
            List<Value> values = value.asList();
            assertTrue(values.isEmpty());
        } catch (Exception e) {
            fail("Unexpected exception occurred.", e);
        }
    }

    @Test
    void valueConstructorValidateListInternals() {
        List<Object> list = new ArrayList<>();
        list.add(new Value("item"));
        list.add("item");

        assertThrows(InstantiationException.class, () -> new Value(list));
    }

    @Test
    void noOpFinalize() {
        Value val = new Value();
        assertDoesNotThrow(val::finalize); // does nothing, but we want to defined in and make it final.
    }

    @Test
    void equalValuesShouldBeEqual() {
        Value val1 = new Value(12312312);
        Value val2 = new Value(12312312);
        assertEquals(val1, val2);
    }

    @Test
    void unequalValuesShouldNotBeEqual() {
        Value val1 = new Value("a");
        Value val2 = new Value("b");
        assertNotEquals(val1, val2);
    }
}

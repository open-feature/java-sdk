package dev.openfeature.javasdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ValueTest {
    @Test public void noArgShouldContainNull() {
        Value value = new Value();
        assertTrue(value.isNull());
    }

    @Test public void boolArgShouldContainBool() {
        boolean innerValue = true;
        Value value = new Value(innerValue);
        assertTrue(value.isBoolean());
        assertEquals(innerValue, value.asBoolean());
    }

    @Test public void numericArgShouldReturnDoubleOrInt() {
        double innerDoubleValue = .75;
        Value doubleValue = new Value(innerDoubleValue);
        assertTrue(doubleValue.isNumber());
        assertEquals(1, doubleValue.asInteger());     // should be rounded
        assertEquals(.75, doubleValue.asDouble());

        int innerIntValue = 100;
        Value intValue = new Value(innerIntValue);
        assertTrue(intValue.isNumber());
        assertEquals(innerIntValue, intValue.asInteger());
        assertEquals(innerIntValue, intValue.asDouble());
    }

    @Test public void stringArgShouldContainString() {
        String innerValue = "hi!";
        Value value = new Value(innerValue);
        assertTrue(value.isString());
        assertEquals(innerValue, value.asString());
    }

    @Test public void dateShouldContainDate() {
        ZonedDateTime innerValue = ZonedDateTime.now();
        Value value = new Value(innerValue);
        assertTrue(value.isZonedDateTime());
        assertEquals(innerValue, value.asZonedDateTime());
    }

    @Test public void structureShouldContainStructure() {
        String INNER_KEY = "key";
        String INNER_VALUE = "val";
        Structure innerValue = new Structure().add(INNER_KEY, INNER_VALUE);
        Value value = new Value(innerValue);
        assertTrue(value.isStructure());
        assertEquals(INNER_VALUE, value.asStructure().getValue(INNER_KEY).asString());
    }

    @Test public void listArgShouldContainList() {
        String ITEM_VALUE = "val";
        List<Value> innerValue = new ArrayList<Value>();
        innerValue.add(new Value(ITEM_VALUE));
        Value value = new Value(innerValue);
        assertTrue(value.isList());
        assertEquals(ITEM_VALUE, value.asList().get(0).asString());
    }
}

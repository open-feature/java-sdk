package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MutableStructureTest {

    @Test
    void emptyMutableStructureIsEmpty() {
        MutableStructure m1 = new MutableStructure();
        assertTrue(m1.isEmpty());
    }

    @Test
    void mutableStructureWithNullBackingStructureIsEmpty() {
        MutableStructure m1 = new MutableStructure(null);
        assertTrue(m1.isEmpty());
    }

    @Test
    void asUnmodifiableMapOnEmptyStructureIsEmpty() {
        Map<String, Value> map = new MutableStructure().asUnmodifiableMap();
        assertThat(map).isEmpty();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> map.put("key", new Value("val")));
    }

    @Test
    void asUnmodifiableMapOnNullAttributesIsEmpty() {
        Map<String, Value> map = new MutableStructure(null).asUnmodifiableMap();
        assertThat(map).isEmpty();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> map.put("key", new Value("val")));
    }

    @Test
    void unequalMutableStructuresAreNotEqual() {
        MutableStructure m1 = new MutableStructure();
        m1.add("key1", "val1");
        MutableStructure m2 = new MutableStructure();
        m2.add("key2", "val2");
        assertNotEquals(m1, m2);
    }

    @Test
    void equalMutableStructuresAreEqual() {
        MutableStructure m1 = new MutableStructure();
        m1.add("key1", "val1");
        MutableStructure m2 = new MutableStructure();
        m2.add("key1", "val1");
        assertEquals(m1, m2);
    }

    @Test
    void equalAbstractStructuresOfDifferentTypesAreNotEqual() {
        MutableStructure m1 = new MutableStructure();
        m1.add("key1", "val1");
        HashMap<String, Value> map = new HashMap<>();
        map.put("key1", new Value("val1"));
        AbstractStructure m2 = new AbstractStructure(map) {
            @Override
            public Set<String> keySet() {
                return attributes.keySet();
            }

            @Override
            public Value getValue(String key) {
                return attributes.get(key);
            }

            @Override
            public Map<String, Value> asMap() {
                return attributes;
            }
        };

        assertNotEquals(m1, m2);
    }
}

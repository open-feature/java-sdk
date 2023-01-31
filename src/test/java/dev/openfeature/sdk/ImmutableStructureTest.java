package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmutableStructureTest {
    @Test void noArgShouldContainEmptyAttributes() {
        ImmutableStructure structure = new ImmutableStructure();
        assertEquals(0, structure.asMap().keySet().size());
    }

    @Test void mapArgShouldContainNewMap() {
        String KEY = "key";
        Map<String, Value> map = new HashMap<String, Value>() {
            {
                put(KEY, new Value(KEY));
            }
        };
        ImmutableStructure structure = new ImmutableStructure(map);
        assertEquals(KEY, structure.asMap().get(KEY).asString());
        assertNotSame(structure.asMap(), map); // should be a copy
    }

    @Test void MutatingGetValueShouldNotChangeOriginalValue() {
        String KEY = "key";
        List<Value> lists = new ArrayList<>();
        lists.add(new Value(KEY));
        Map<String, Value> map = new HashMap<String, Value>() {
            {
                put(KEY, new Value(lists));
            }
        };
        ImmutableStructure structure = new ImmutableStructure(map);
        List<Value> values = structure.getValue(KEY).asList();
        values.add(new Value("dummyValue"));
        lists.add(new Value("dummy"));
        assertEquals(1, structure.getValue(KEY).asList().size());
        assertNotSame(structure.asMap(), map); // should be a copy
    }

    @Test void MutatingGetInstantValueShouldNotChangeOriginalValue() {
        String KEY = "key";
        Instant now = Instant.now();
        Map<String, Value> map = new HashMap<String, Value>() {
            {
                put(KEY, new Value(now));
            }
        };
        ImmutableStructure structure = new ImmutableStructure(map);
        //mutate the original value
        Instant tomorrow = now.plus(1, ChronoUnit.DAYS);
        //mutate the getValue
        structure.getValue(KEY).asInstant().plus(1, ChronoUnit.DAYS);

        assertNotEquals(tomorrow, structure.getValue(KEY).asInstant());
        assertEquals(now, structure.getValue(KEY).asInstant());
    }

    @Test void MutatingGetStructureValueShouldNotChangeOriginalValue() {
        String KEY = "key";
        List<Value> lists = new ArrayList<>();
        lists.add(new Value("dummy_list_1"));
        MutableStructure mutableStructure = new MutableStructure().add("key1","val1").add("list", lists);
        Map<String, Value> map = new HashMap<String, Value>() {
            {
                put(KEY, new Value(mutableStructure));
            }
        };
        ImmutableStructure structure = new ImmutableStructure(map);
        //mutate the original structure
        mutableStructure.add("key2", "val2");
        //mutate the return value
        structure.getValue(KEY).asStructure().asMap().put("key3", new Value("val3"));
        assertEquals(2, structure.getValue(KEY).asStructure().asMap().size());
        assertArrayEquals(new Object[]{"key1", "list"}, structure.getValue(KEY).asStructure().keySet().toArray());
        assertTrue(structure.getValue(KEY).asStructure() instanceof ImmutableStructure);
        //mutate list value
        lists.add(new Value("dummy_list_2"));
        //mutate the return list value
        structure.getValue(KEY).asStructure().asMap().get("list").asList().add(new Value("dummy_list_3"));
        assertEquals(1, structure.getValue(KEY).asStructure().asMap().get("list").asList().size());
        assertEquals("dummy_list_1", structure.getValue(KEY).asStructure().asMap().get("list").asList().get(0).asString());
    }

    @Test
    void ModifyingTheValuesReturnByTheKeySetMethodShouldNotModifyTheUnderlyingImmutableStructure() {
        Map<String, Value> map = new HashMap<String, Value>() {
            {
                put("key", new Value(10));
                put("key1", new Value(20));
            }
        };
        ImmutableStructure structure = new ImmutableStructure(map);
        Set<String> keys = structure.keySet();
        keys.remove("key1");
        assertEquals(2, structure.keySet().size());
    }
}

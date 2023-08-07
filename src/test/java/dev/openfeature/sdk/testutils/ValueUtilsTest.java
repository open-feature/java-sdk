package dev.openfeature.sdk.testutils;

import dev.openfeature.sdk.Value;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueUtilsTest {

    @Getter
    @Builder
    public static class Struct {
        private int i;
        private String s;
        private Double d;
        private Map<String, Map<String, Int>> map = new HashMap<>();
        private List<List<Int>> list = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class Int {
        private int value;
    }

    @SneakyThrows
    @Test
    public void testObject() {
        Map<String, Map<String, Int>> map = new HashMap<>();
        Map<String, Int> innerMap = new HashMap<>();
        innerMap.put("innerKey1", new Int(4));
        map.put("key1", innerMap);
        List<Int> innerList = new ArrayList<>();
        innerList.add(new Int(456));
        List<List<Int>> list = new ArrayList<>();
        list.add(innerList);
        Struct struct = Struct.builder().i(3).d(1.2).s("str").map(map).list(list).build();
        Value value = ValueUtils.convert(struct);
        assertEquals("Value(innerObject=MutableStructure(attributes={i=Value(innerObject=3), s=Value(innerObject=str), d=Value(innerObject=1.2), list=Value(innerObject=[Value(innerObject=[Value(innerObject=MutableStructure(attributes={value=Value(innerObject=456)}))])]), map=Value(innerObject=MutableStructure(attributes={key1=Value(innerObject=MutableStructure(attributes={innerKey1=Value(innerObject=MutableStructure(attributes={value=Value(innerObject=4)}))}))}))}))", value.toString());
    }
}
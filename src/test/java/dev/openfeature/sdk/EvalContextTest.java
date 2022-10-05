package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class EvalContextTest {
    @Specification(number="3.1.1",
            text="The `evaluation context` structure **MUST** define an optional `targeting key` field of " +
                    "type string, identifying the subject of the flag evaluation.")
    @Test void requires_targeting_key() {
        MutableContext ec = new MutableContext();
        ec.setTargetingKey("targeting-key");
        assertEquals("targeting-key", ec.getTargetingKey());
    }

    @Specification(number="3.1.2", text="The evaluation context MUST support the inclusion of " +
            "custom fields, having keys of type `string`, and " +
            "values of type `boolean | string | number | datetime | structure`.")
    @Test void eval_context() {
        MutableContext ec = new MutableContext();

        ec.add("str", "test");
        assertEquals("test", ec.getValue("str").asString());

        ec.add("bool", true);
        assertEquals(true, ec.getValue("bool").asBoolean());

        ec.add("int", 4);
        assertEquals(4, ec.getValue("int").asInteger());

        Instant dt = Instant.now();
        ec.add("dt", dt);
        assertEquals(dt, ec.getValue("dt").asInstant());
    }

    @Specification(number="3.1.2", text="The evaluation context MUST support the inclusion of " +
            "custom fields, having keys of type `string`, and " +
            "values of type `boolean | string | number | datetime | structure`.")
    @Test void eval_context_structure_array() {
        MutableContext ec = new MutableContext();
        ec.add("obj", new HashMapStructure().add("val1", 1).add("val2", "2"));
        ec.add("arr", new ArrayList<Value>(){{
            add(new Value("one"));
            add(new Value("two"));
        }});

        HashMapStructure str = ec.getValue("obj").asStructure();
        assertEquals(1, str.getValue("val1").asInteger());
        assertEquals("2", str.getValue("val2").asString());

        List<Value> arr = ec.getValue("arr").asList();
        assertEquals("one", arr.get(0).asString());
        assertEquals("two", arr.get(1).asString());
    }

    @Specification(number="3.1.3", text="The evaluation context MUST support fetching the custom fields by key and also fetching all key value pairs.")
    @Test void fetch_all() {
        MutableContext ec = new MutableContext();

        ec.add("str", "test");
        ec.add("str2", "test2");

        ec.add("bool", true);
        ec.add("bool2", false);

        ec.add("int", 4);
        ec.add("int2", 2);

        Instant dt = Instant.now();
        ec.add("dt", dt);

        ec.add("obj", new HashMapStructure().add("val1", 1).add("val2", "2"));

        Map<String, Value> foundStr = ec.asMap();
        assertEquals(ec.getValue("str").asString(), foundStr.get("str").asString());
        assertEquals(ec.getValue("str2").asString(), foundStr.get("str2").asString());

        Map<String, Value> foundBool = ec.asMap();
        assertEquals(ec.getValue("bool").asBoolean(), foundBool.get("bool").asBoolean());
        assertEquals(ec.getValue("bool2").asBoolean(), foundBool.get("bool2").asBoolean());

        Map<String, Value> foundInt = ec.asMap();
        assertEquals(ec.getValue("int").asInteger(), foundInt.get("int").asInteger());
        assertEquals(ec.getValue("int2").asInteger(), foundInt.get("int2").asInteger());

        HashMapStructure foundObj = ec.getValue("obj").asStructure();
        assertEquals(1, foundObj.getValue("val1").asInteger());
        assertEquals("2", foundObj.getValue("val2").asString());
    }

    @Specification(number="3.1.4", text="The evaluation context fields MUST have an unique key.")
    @Test void unique_key_across_types() {
        MutableContext ec = new MutableContext();
        ec.add("key", "val");
        ec.add("key", "val2");
        assertEquals("val2", ec.getValue("key").asString());
        ec.add("key", 3);
        assertEquals(null, ec.getValue("key").asString());
        assertEquals(3, ec.getValue("key").asInteger());
    }

    @Test void can_chain_attribute_addition() {
        MutableContext ec = new MutableContext();
        MutableContext out = ec.add("str", "test")
                .add("int", 4)
                .add("bool", false)
                .add("str", new HashMapStructure());
        assertEquals(MutableContext.class, out.getClass());
    }

    @Test void can_add_key_with_null() {
        MutableContext ec = new MutableContext()
                .add("Boolean", (Boolean)null)
                .add("String", (String)null)
                .add("Double", (Double)null)
                .add("Structure", (HashMapStructure)null)
                .add("List", (List<Value>)null)
                .add("Instant", (Instant)null);
        assertEquals(6, ec.asMap().size());
        assertEquals(null, ec.getValue("Boolean").asBoolean());
        assertEquals(null, ec.getValue("String").asString());
        assertEquals(null, ec.getValue("Double").asDouble());
        assertEquals(null, ec.getValue("Structure").asStructure());
        assertEquals(null, ec.getValue("List").asList());
        assertEquals(null, ec.getValue("Instant").asString());
    }

    @Test void merge_targeting_key() {
        String key1 = "key1";
        EvaluationContext ctx1 = new MutableContext(key1);
        EvaluationContext ctx2 = new MutableContext();

        EvaluationContext ctxMerged = ctx1.merge(ctx2);
        assertEquals(key1, ctxMerged.getTargetingKey());

        String key2 = "key2";
        ctx2.setTargetingKey(key2);
        ctxMerged = ctx1.merge(ctx2);
        assertEquals(key2, ctxMerged.getTargetingKey());

        ctx2.setTargetingKey("  ");
        ctxMerged = ctx1.merge(ctx2);
        assertEquals(key1, ctxMerged.getTargetingKey());
    }

    @Test void asObjectMap() {
        String key1 = "key1";
        MutableContext ctx = new MutableContext(key1);
        ctx.add("stringItem", "stringValue");
        ctx.add("boolItem", false);
        ctx.add("integerItem", 1);
        ctx.add("doubleItem", 1.2);
        ctx.add("instantItem",  Instant.ofEpochSecond(1663331342));
        List<Value> listItem = new ArrayList<>();
        listItem.add(new Value("item1"));
        listItem.add(new Value("item2"));
        ctx.add("listItem", listItem);
        List<Value> listItem2 = new ArrayList<>();
        listItem2.add(new Value(true));
        listItem2.add(new Value(false));
        ctx.add("listItem2", listItem2);
        Map<String, Value> structureValue = new HashMap<>();
        structureValue.put("structStringItem", new Value("stringValue"));
        structureValue.put("structBoolItem", new Value(false));
        structureValue.put("structIntegerItem", new Value(1));
        structureValue.put("structDoubleItem", new Value(1.2));
        structureValue.put("structInstantItem",  new Value(Instant.ofEpochSecond(1663331342)));
        HashMapStructure structure = new HashMapStructure(structureValue);
        ctx.add("structureItem", structure);


        Map<String, Object> want = new HashMap<>();
        want.put("stringItem", "stringValue");
        want.put("boolItem", false);
        want.put("integerItem", 1);
        want.put("doubleItem", 1.2);
        want.put("instantItem",  Instant.ofEpochSecond(1663331342));
        List<String> wantListItem = new ArrayList<>();
        wantListItem.add("item1");
        wantListItem.add("item2");
        want.put("listItem", wantListItem);
        List<Boolean> wantListItem2 = new ArrayList<>();
        wantListItem2.add(true);
        wantListItem2.add(false);
        want.put("listItem2", wantListItem2);
        Map<String, Object> wantStructureValue = new HashMap<>();
        wantStructureValue.put("structStringItem", "stringValue");
        wantStructureValue.put("structBoolItem", false);
        wantStructureValue.put("structIntegerItem", 1);
        wantStructureValue.put("structDoubleItem", 1.2);
        wantStructureValue.put("structInstantItem",  Instant.ofEpochSecond(1663331342));
        want.put("structureItem",wantStructureValue);

        assertEquals(want,ctx.asObjectMap());
    }
}

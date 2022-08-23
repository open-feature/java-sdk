package dev.openfeature.javasdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class EvalContextTest {
    @Specification(number="3.1.1",
            text="The `evaluation context` structure **MUST** define an optional `targeting key` field of " +
                    "type string, identifying the subject of the flag evaluation.")
    @Test void requires_targeting_key() {
        EvaluationContext ec = new EvaluationContext();
        ec.setTargetingKey("targeting-key");
        assertEquals("targeting-key", ec.getTargetingKey());
    }

    @Specification(number="3.1.2", text="The evaluation context MUST support the inclusion of " +
            "custom fields, having keys of type `string`, and " +
            "values of type `boolean | string | number | datetime | structure`.")
    @Test void eval_context() {
        EvaluationContext ec = new EvaluationContext();

        ec.add("str", "test");
        assertEquals("test", ec.getStringAttribute("str"));

        ec.add("bool", true);
        assertEquals(true, ec.getBooleanAttribute("bool"));

        ec.add("int", 4);
        assertEquals(4, ec.getIntegerAttribute("int"));

        ZonedDateTime dt = ZonedDateTime.now();
        ec.add("dt", dt);
        assertEquals(dt, ec.getDatetimeAttribute("dt"));
    }

    @Specification(number="3.1.2", text="The evaluation context MUST support the inclusion of " +
            "custom fields, having keys of type `string`, and " +
            "values of type `boolean | string | number | datetime | structure`.")
    @Test void eval_context_structure_array() {
        EvaluationContext ec = new EvaluationContext();
        ec.add("obj", new Structure().add("val1", 1).add("val2", "2"));
        ec.add("arr", new ArrayList<String>(){{
            add(0, "one");
            add(1, "two");
        }});

        Structure str = ec.getStructureAttribute("obj");
        assertEquals(1, str.getIntegerAttribute("val1"));
        assertEquals("2", str.getStringAttribute("val2"));

        List<String> arr = ec.getArrayAttribute("arr");
        assertEquals("one", arr.get(0));
        assertEquals("two", arr.get(1));
    }

    @Specification(number="3.1.3", text="The evaluation context MUST support fetching the custom fields by key and also fetching all key value pairs.")
    @Test void fetch_all() {
        EvaluationContext ec = new EvaluationContext();

        ec.add("str", "test");
        ec.add("str2", "test2");

        ec.add("bool", true);
        ec.add("bool2", false);

        ec.add("int", 4);
        ec.add("int2", 2);

        ZonedDateTime dt = ZonedDateTime.now();
        ec.add("dt", dt);

        ec.add("obj", new Structure().add("val1", 1).add("val2", "2"));

        Map<String, Object> foundStr = ec.getAllAttributes();
        assertEquals(ec.getStringAttribute("str"), foundStr.get("str"));
        assertEquals(ec.getStringAttribute("str2"), foundStr.get("str2"));

        Map<String, Object> foundBool = ec.getAllAttributes();
        assertEquals(ec.getBooleanAttribute("bool"), foundBool.get("bool"));
        assertEquals(ec.getBooleanAttribute("bool2"), foundBool.get("bool2"));

        Map<String, Object> foundInt = ec.getAllAttributes();
        assertEquals(ec.getIntegerAttribute("int"), foundInt.get("int"));
        assertEquals(ec.getIntegerAttribute("int2"), foundInt.get("int2"));

        Structure foundObj = ec.getStructureAttribute("obj");
        assertEquals(1, foundObj.getIntegerAttribute("val1"));
        assertEquals("2", foundObj.getStringAttribute("val2"));
    }

    @Specification(number="3.1.4", text="The evaluation context fields MUST have an unique key.")
    @Test void unique_key_across_types() {
        EvaluationContext ec = new EvaluationContext();
        ec.add("key", "val");
        ec.add("key", "val2");
        assertEquals("val2", ec.getStringAttribute("key"));
        ec.add("key", 3);
        assertEquals(null, ec.getStringAttribute("key"));
        assertEquals(3, ec.getIntegerAttribute("key"));
    }

    @Test void can_chain_attribute_addition() {
        EvaluationContext ec = new EvaluationContext();
        Structure out = ec.add("str", "test")
                .add("int", 4)
                .add("bool", false)
                .add("str", new Structure());
        assertEquals(EvaluationContext.class, out.getClass());
    }

    @Test void merge_targeting_key() {
        String key1 = "key1";
        EvaluationContext ctx1 = new EvaluationContext(key1);
        EvaluationContext ctx2 = new EvaluationContext();

        EvaluationContext ctxMerged = EvaluationContext.merge(ctx1, ctx2);
        assertEquals(key1, ctxMerged.getTargetingKey());

        String key2 = "key2";
        ctx2.setTargetingKey(key2);
        ctxMerged = EvaluationContext.merge(ctx1, ctx2);
        assertEquals(key2, ctxMerged.getTargetingKey());

        ctx2.setTargetingKey("  ");
        ctxMerged = EvaluationContext.merge(ctx1, ctx2);
        assertEquals(key1, ctxMerged.getTargetingKey());
    }
}

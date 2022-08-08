package dev.openfeature.javasdk;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

        ec.addStringAttribute("str", "test");
        assertEquals("test", ec.getStringAttribute("str"));

        ec.addBooleanAttribute("bool", true);
        assertEquals(true, ec.getBooleanAttribute("bool"));

        ec.addIntegerAttribute("int", 4);
        assertEquals(4, ec.getIntegerAttribute("int"));

        ZonedDateTime dt = ZonedDateTime.now();
        ec.addDatetimeAttribute("dt", dt);
        assertEquals(dt, ec.getDatetimeAttribute("dt"));
    }

    @Specification(number="3.1.2", text="The evaluation context MUST support the inclusion of " +
            "custom fields, having keys of type `string`, and " +
            "values of type `boolean | string | number | datetime | structure`.")
    @Test void eval_context__structure() {
        Node<Integer> n1 = new Node<>();
        n1.value = 4;
        Node<Integer> n2 = new Node<>();
        n2.value = 2;
        n2.left = n1;

        EvaluationContext ec = new EvaluationContext();
        ec.addStructureAttribute("obj", n2);

        Node nodeFromString = ec.getStructureAttribute("obj");
        assertEquals(n2, nodeFromString);
        assertEquals(n1, nodeFromString.left);
        assertEquals(2, nodeFromString.value);
        assertEquals(4, nodeFromString.left.value);
    }

    @Specification(number="3.1.3", text="The evaluation context MUST support fetching the custom fields by key and also fetching all key value pairs.")
    @Test void fetch_all() {
        EvaluationContext ec = new EvaluationContext();

        ec.addStringAttribute("str", "test");
        ec.addStringAttribute("str2", "test2");

        ec.addBooleanAttribute("bool", true);
        ec.addBooleanAttribute("bool2", false);

        ec.addIntegerAttribute("int", 4);
        ec.addIntegerAttribute("int2", 2);

        ZonedDateTime dt = ZonedDateTime.now();
        ec.addDatetimeAttribute("dt", dt);

        Node<Integer> n1 = new Node<>();
        n1.value = 4;
        Node<Integer> n2 = new Node<>();
        n2.value = 2;
        n2.left = n1;
        ec.addStructureAttribute("obj", n2);

        Map<String, String> foundStr = ec.getStringAttributes();
        assertEquals(ec.getStringAttribute("str"), foundStr.get("str"));
        assertEquals(ec.getStringAttribute("str2"), foundStr.get("str2"));

        Map<String, Boolean> foundBool = ec.getBooleanAttributes();
        assertEquals(ec.getBooleanAttribute("bool"), foundBool.get("bool"));
        assertEquals(ec.getBooleanAttribute("bool2"), foundBool.get("bool2"));

        Map<String, Integer> foundInt = ec.getIntegerAttributes();
        assertEquals(ec.getIntegerAttribute("int"), foundInt.get("int"));
        assertEquals(ec.getIntegerAttribute("int2"), foundInt.get("int2"));

        Map<String, String> foundObj = ec.getStructureAttributes();
        assertEquals(ec.<Node>getStructureAttribute("obj"), n2);
    }

    @Specification(number="3.1.4", text="The evaluation context fields MUST have an unique key.")
    @Test void unique_key_across_types() {
        EvaluationContext ec = new EvaluationContext();
        ec.addStringAttribute("key", "val");
        ec.addStringAttribute("key", "val2");
        assertEquals("val2", ec.getStringAttribute("key"));
        ec.addIntegerAttribute("key", 3);
        assertEquals(null, ec.getStringAttribute("key"));
        assertEquals(3, ec.getIntegerAttribute("key"));
    }

    @Test void can_chain_attribute_addition() {
        EvaluationContext ec = new EvaluationContext();
        EvaluationContext out = ec.addStructureAttribute("str", "test")
                .addIntegerAttribute("int", 4)
                .addBooleanAttribute("bool", false)
                .addStructureAttribute("str", new Node<Integer>());
        assertEquals(EvaluationContext.class, out.getClass());
    }
}

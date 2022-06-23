package dev.openfeature.javasdk;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvalContextTests {
    @Specification(number="3.1",
            text="The `evaluation context` structure **MUST** define an optional `targeting key` field of " +
                    "type string, identifying the subject of the flag evaluation.")
    @Test void requires_targeting_key() {
        EvaluationContext ec = new EvaluationContext();
        ec.setTargetingKey("targeting-key");
        assertEquals("targeting-key", ec.getTargetingKey());
    }

    @Specification(number="3.2", text="The evaluation context MUST support the inclusion of " +
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

    @Specification(number="3.2", text="The evaluation context MUST support the inclusion of " +
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


        String stringyObject = ec.jsonAttributes.get("obj");
        assertEquals("{\"left\":{\"left\":null,\"right\":null,\"value\":4},\"right\":null,\"value\":2}", stringyObject);

        Node nodeFromString = ec.getStructureAttribute("obj", Node.class);
        assertEquals(n2, nodeFromString);
        assertEquals(n1, nodeFromString.left);
        assertEquals(2, nodeFromString.value);
        assertEquals(4, nodeFromString.left.value);
    }
}

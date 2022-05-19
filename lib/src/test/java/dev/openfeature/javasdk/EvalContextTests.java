package dev.openfeature.javasdk;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EvalContextTests {
    @Specification(spec="flag evaluation", number="3.1",
            text="The `evaluation context` structure MUST define a required `targeting key` " +
                    "field of type string, identifying the subject of the flag evaluation.")
    @Disabled("https://github.com/open-feature/spec/pull/60/files#r872827439")
    @Test void requires_targeting_key() {
        EvaluationContext ec = new EvaluationContext();
        assertEquals("targeting-key", ec.getTargetingKey());
    }

    @Specification(spec="flag evaluation", number="3.3", text="The evaluation context MUST support the inclusion " +
            "of custom fields, having keys of type `string`, and values of " +
            "type `boolean | string | number | datetime`.")
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


    @Specification(spec="flag evaluation", number="3.2", text="The evaluation context MUST define the " +
            "following optional fields: `email` (string), `first name` (string), `last name`(string), " +
            "`name`(string), `ip`(string), `tz`(string), `locale`(string), `country code` (string), " +
            "`timestamp`(date), `environment`(string), `application`(string), and `version`(string).")
    @Test void mandated_fields() {
        EvaluationContext ec = new EvaluationContext();

        assertNull(ec.getEmail());
        ec.setEmail("Test");
        assertEquals("Test", ec.getEmail());

        assertNull(ec.getFirstName());
        ec.setFirstName("Test");
        assertEquals("Test", ec.getFirstName());

        assertNull(ec.getLastName());
        ec.setLastName("Test");
        assertEquals("Test", ec.getLastName());

        assertNull(ec.getName());
        ec.setName("Test");
        assertEquals("Test", ec.getName());

        assertNull(ec.getIp());
        ec.setIp("Test");
        assertEquals("Test", ec.getIp());

        assertNull(ec.getTz());
        ec.setTz("Test");
        assertEquals("Test", ec.getTz());

        assertNull(ec.getLocale());
        ec.setLocale("Test");
        assertEquals("Test", ec.getLocale());

        assertNull(ec.getCountryCode());
        ec.setCountryCode("Test");
        assertEquals("Test", ec.getCountryCode());

        assertNull(ec.getTimestamp());
        ZonedDateTime dt = ZonedDateTime.now();
        ec.setTimestamp(dt);
        assertEquals(dt, ec.getTimestamp());

        assertNull(ec.getEnvironment());
        ec.setEnvironment("Test");
        assertEquals("Test", ec.getEnvironment());


        assertNull(ec.getApplication());
        ec.setApplication("Test");
        assertEquals("Test", ec.getApplication());


        assertNull(ec.getVersion());
        ec.setVersion("Test");
        assertEquals("Test", ec.getVersion());
    }
}

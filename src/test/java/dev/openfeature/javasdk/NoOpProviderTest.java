package dev.openfeature.javasdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NoOpProviderTest {
    @Test void bool() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Boolean> eval = p.getBooleanEvaluation("key", true, null);
        assertEquals(true, eval.getValue());
    }

    @Test void str() {
        NoOpProvider p = new NoOpProvider();

        ProviderEvaluation<String> eval = p.getStringEvaluation("key", "works", null);
        assertEquals("works", eval.getValue());
    }

    @Test void integer() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Integer> eval = p.getIntegerEvaluation("key", 4, null);
        assertEquals(4, eval.getValue());
    }

    @Test void noOpdouble() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Double> eval = p.getDoubleEvaluation("key", 0.4, null);
        assertEquals(0.4, eval.getValue());
    }

    @Test void structure() {
        NoOpProvider p = new NoOpProvider();
        Structure s = new Structure();
        ProviderEvaluation<Structure> eval = p.getObjectEvaluation("key", s, null);
        assertEquals(s, eval.getValue());
    }
}

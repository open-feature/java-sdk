package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NoOpProviderTest {
    @Test
    void bool() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Boolean> eval = p.getBooleanEvaluation("key", true, null);
        assertEquals(true, eval.getValue());
    }

    @Test
    void str() {
        NoOpProvider p = new NoOpProvider();

        ProviderEvaluation<String> eval = p.getStringEvaluation("key", "works", null);
        assertEquals("works", eval.getValue());
    }

    @Test
    void integer() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Integer> eval = p.getIntegerEvaluation("key", 4, null);
        assertEquals(4, eval.getValue());
    }

    @Test
    void noOpdouble() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Double> eval = p.getDoubleEvaluation("key", 0.4, null);
        assertEquals(0.4, eval.getValue());
    }

    @Test
    void value() {
        NoOpProvider p = new NoOpProvider();
        Value s = new Value();
        ProviderEvaluation<Value> eval = p.getObjectEvaluation("key", s, null);
        assertEquals(s, eval.getValue());
    }

    @Test
    void noOpNumber() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Number> eval = p.getNumberEvaluation("key", 123456789L, null);
        assertEquals(123456789.0, eval.getValue());
    }
}

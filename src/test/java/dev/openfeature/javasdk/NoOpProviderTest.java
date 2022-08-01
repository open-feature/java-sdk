package dev.openfeature.javasdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoOpProviderTest {
    @Test void bool() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Boolean> eval = p.getBooleanEvaluation("key", true, null, null);
        assertEquals(true, eval.getValue());
    }

    @Test void str() {
        NoOpProvider p = new NoOpProvider();

        ProviderEvaluation<String> eval = p.getStringEvaluation("key", "works", null, null);
        assertEquals("works", eval.getValue());
    }

    @Test void integer() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Integer> eval = p.getIntegerEvaluation("key", 4, null, null);
        assertEquals(4, eval.getValue());
    }

    @Test void noOpdouble() {
        NoOpProvider p = new NoOpProvider();
        ProviderEvaluation<Double> eval = p.getDoubleEvaluation("key", 0.4, null, null);
        assertEquals(0.4, eval.getValue());
    }

    @Test void structure() {
        NoOpProvider p = new NoOpProvider();
        Node<Integer> node = new Node<Integer>();
        ProviderEvaluation<Node> eval = p.getObjectEvaluation("key", node, null, null);
        assertEquals(node, eval.getValue());
    }
}

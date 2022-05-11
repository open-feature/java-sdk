package javasdk;

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
}

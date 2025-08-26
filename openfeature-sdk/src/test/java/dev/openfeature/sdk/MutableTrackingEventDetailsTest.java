package dev.openfeature.sdk;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import dev.openfeature.api.MutableContext;
import dev.openfeature.api.MutableTrackingEventDetails;
import dev.openfeature.api.Value;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MutableTrackingEventDetailsTest {

    @Test
    void hasDefaultValue() {
        MutableTrackingEventDetails track = new MutableTrackingEventDetails();
        assertFalse(track.getValue().isPresent());
    }

    @Test
    void shouldUseCorrectValue() {
        MutableTrackingEventDetails track = new MutableTrackingEventDetails(3);
        assertThat(track.getValue()).hasValue(3);
    }

    @Test
    void shouldStoreAttributes() {
        MutableTrackingEventDetails track = new MutableTrackingEventDetails();
        track.add("key0", true);
        track.add("key1", 1);
        track.add("key2", "2");
        track.add("key3", 1d);
        track.add("key4", 4);
        track.add("key5", Instant.parse("2023-12-03T10:15:30Z"));
        track.add("key6", new MutableContext());
        track.add("key7", new Value(7));
        track.add("key8", Lists.newArrayList(new Value(8), new Value(9)));

        assertEquals(new Value(true), track.getValue("key0"));
        assertEquals(new Value(1), track.getValue("key1"));
        assertEquals(new Value("2"), track.getValue("key2"));
        assertEquals(new Value(1d), track.getValue("key3"));
        assertEquals(new Value(4), track.getValue("key4"));
        assertEquals(new Value(Instant.parse("2023-12-03T10:15:30Z")), track.getValue("key5"));
        assertEquals(new Value(new MutableContext()), track.getValue("key6"));
        assertEquals(new Value(7), track.getValue("key7"));
        assertArrayEquals(
                new Object[] {new Value(8), new Value(9)},
                track.getValue("key8").asList().toArray());
    }
}

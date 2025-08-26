package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.fail;

import dev.openfeature.api.Metadata;
import org.junit.jupiter.api.Test;

class MetadataTest {
    @Specification(
            number = "4.2.2.2",
            text = "Condition: The client metadata field in the hook context MUST be immutable.")
    @Specification(
            number = "4.2.2.3",
            text = "Condition: The provider metadata field in the hook context MUST be immutable.")
    @Test
    void metadata_is_immutable() {
        try {
            Metadata.class.getMethod("setName", String.class);
            fail("Not expected to be mutable.");
        } catch (NoSuchMethodException e) {
            // Pass
        }
    }
}

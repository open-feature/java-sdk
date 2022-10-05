package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class MetadataTest {
    @Specification(number="4.2.2.2", text="Condition: The client metadata field in the hook context MUST be immutable.")
    @Specification(number="4.2.2.3", text="Condition: The provider metadata field in the hook context MUST be immutable.")
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
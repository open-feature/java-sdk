package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class OpenFeatureAPISingeltonTest {

    @Specification(
            number = "1.1.1",
            text =
                    "The API, and any state it maintains SHOULD exist as a global singleton, even in cases wherein multiple versions of the API are present at runtime.")
    @Test
    void global_singleton() {
        assertSame(DefaultOpenFeatureAPI.getInstance(), DefaultOpenFeatureAPI.getInstance());
    }
}

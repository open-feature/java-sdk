package dev.openfeature.sdk.testutils.exception;

public class TestException extends RuntimeException {

    @Override
    public String getMessage() {
        return "don't panic, it's just a test";
    }
}

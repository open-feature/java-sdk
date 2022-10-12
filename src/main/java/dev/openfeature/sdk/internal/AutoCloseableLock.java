package dev.openfeature.sdk.internal;

public interface AutoCloseableLock extends AutoCloseable {
    
    /**
     * Override the exception in AutoClosable.
     */
    @Override
    void close();
}

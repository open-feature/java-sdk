package dev.openfeature.sdk.internal;

/**
 * Interface for AutoCloseable that doesn't throw.
 */
public interface AutoCloseableLock extends AutoCloseable {
    
    /**
     * Override the exception in AutoClosable.
     */
    @Override
    void close();
}

package dev.openfeature.sdk.internal;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface AutoCloseableLock extends AutoCloseable {
    
    /**
     * Override the exception in AutoClosable.
     */
    @Override
    void close();
}

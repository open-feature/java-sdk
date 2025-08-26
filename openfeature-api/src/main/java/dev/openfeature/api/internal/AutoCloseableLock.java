package dev.openfeature.api.internal;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface AutoCloseableLock extends AutoCloseable {

    /**
     * Override the exception in AutoClosable.
     */
    @Override
    void close();
}

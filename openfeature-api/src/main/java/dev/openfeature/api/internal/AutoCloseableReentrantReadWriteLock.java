package dev.openfeature.api.internal;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A utility class that wraps a multi-read/single-write lock construct as AutoCloseable, so it can
 * be used in a try-with-resources.
 */
public class AutoCloseableReentrantReadWriteLock extends ReentrantReadWriteLock {

    /**
     * Get the single write lock as an AutoCloseableLock.
     *
     * @return unlock method ref
     */
    public AutoCloseableLock writeLockAutoCloseable() {
        this.writeLock().lock();
        return this.writeLock()::unlock;
    }

    /**
     * Get the multi read lock as an AutoCloseableLock.
     *
     * @return unlock method ref
     */
    public AutoCloseableLock readLockAutoCloseable() {
        this.readLock().lock();
        return this.readLock()::unlock;
    }
}

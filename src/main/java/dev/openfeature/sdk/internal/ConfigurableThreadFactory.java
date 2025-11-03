package dev.openfeature.sdk.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A configurable thread factory for internal use in the SDK.
 * Allows daemon or non-daemon threads to be created with a custom name prefix,
 * and a delegate ThreadFactory if required.
 */
public final class ConfigurableThreadFactory implements ThreadFactory {

    private final AtomicInteger counter = new AtomicInteger();
    private final String namePrefix;
    private final ThreadFactory delegate;
    private final boolean daemon;

    /**
     * {@link ConfigurableThreadFactory}'s constructor.
     *
     * @param namePrefix    Prefix used for setting the new thread's name.
     */
    public ConfigurableThreadFactory(String namePrefix) {
        this(namePrefix, Executors.defaultThreadFactory(), false);
    }

    /**
     * {@link ConfigurableThreadFactory}'s constructor.
     *
     * @param namePrefix    Prefix used for setting the new thread's name.
     * @param daemon        Whether daemon or non-daemon threads will be created.
     */
    public ConfigurableThreadFactory(String namePrefix, boolean daemon) {
        this(namePrefix, Executors.defaultThreadFactory(), daemon);
    }

    /**
     * {@link ConfigurableThreadFactory}'s constructor.
     *
     * @param namePrefix    Prefix used for setting the new thread's name.
     * @param delegate      Delegate ThreadFactory for creating threads.
     * @param daemon        Whether daemon or non-daemon threads will be created.
     */
    public ConfigurableThreadFactory(String namePrefix, ThreadFactory delegate, boolean daemon) {
        this.namePrefix = namePrefix;
        this.delegate = delegate;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = delegate.newThread(runnable);
        thread.setDaemon(daemon);
        thread.setName(namePrefix + "-" + counter.incrementAndGet());
        return thread;
    }
}

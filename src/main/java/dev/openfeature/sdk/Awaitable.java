package dev.openfeature.sdk;

/**
 * A class to help with synchronization.
 */
public class Awaitable {
    public static final Awaitable FINISHED = new Awaitable(true);

    private boolean isDone = false;

    public Awaitable() {}

    private Awaitable(boolean isDone) {
        this.isDone = isDone;
    }

    /**
     * Lets the calling thread wait until some other thread calls {@link Awaitable#wakeup()}. If
     * {@link Awaitable#wakeup()} has been called before the current thread invokes this method, it will return
     * immediately.
     */
    @SuppressWarnings("java:S2142")
    public void await() {
        if (isDone) {
            return;
        }
        synchronized (this) {
            while (!isDone) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                    // ignored, do not propagate the interrupted state
                }
            }
        }
    }

    /**
     * Wakes up all threads that have called {@link Awaitable#await()} and lets them proceed.
     */
    public synchronized void wakeup() {
        isDone = true;
        this.notifyAll();
    }
}

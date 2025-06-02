package dev.openfeature.sdk;

public class Awaitable {
    public static final Awaitable FINISHED = new Awaitable(true);

    private boolean isDone = false;

    public Awaitable() {}

    private Awaitable(boolean isDone) {
        this.isDone = isDone;
    }

    public void await() {
        if (isDone) {
            return;
        }
        synchronized (this) {
            while (!isDone) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public synchronized void wakeup() {
        isDone = true;
        this.notifyAll();
    }
}

package dev.openfeature.javasdk;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * A {@link FeatureProvider} that simply returns the default values passed to it.
 */
public class EventFiringProvider extends NoOpProvider implements EventAwareFeatureProvider {
    private Consumer<String> eventConsumer;

    public EventFiringProvider() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(eventConsumer != null) {
                    eventConsumer.accept("Event was triggered from the provider");
                }
            }
        }, 0, 1000);
    }

    @Override
    public void setEventHookCallback(Consumer<String> eventHookConsumer) {
        // maybe it's best to wrap this up in the constructor?
        this.eventConsumer = eventHookConsumer;
    }
}

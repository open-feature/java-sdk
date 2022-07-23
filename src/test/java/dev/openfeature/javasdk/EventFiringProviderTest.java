package dev.openfeature.javasdk;

import org.junit.jupiter.api.Test;

class EventFiringProviderTest {
    @Test
    public void test() throws InterruptedException {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.addEventHooks(new EventHook() {
            @Override
            public void onEvent(String event) {
                System.out.println(event);
            }
        });
        api.setProvider(new EventFiringProvider());

        Thread.sleep(10_000);
        // You should see "Event was triggered from the provider" scrolling in stdout
    }
}

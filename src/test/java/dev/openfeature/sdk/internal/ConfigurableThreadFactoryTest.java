package dev.openfeature.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfigurableThreadFactoryTest {

    private static final String THREAD_NAME = "testthread";
    private final Runnable runnable = () -> {};

    @Test
    void verifyNewThreadHasNamePrefix() {

        var configurableThreadFactory = new ConfigurableThreadFactory(THREAD_NAME);
        var thread = configurableThreadFactory.newThread(runnable);

        assertThat(thread.getName()).isEqualTo(THREAD_NAME + "-1");
        assertThat(thread.isDaemon()).isFalse();
    }

    @Test
    void verifyNewThreadHasNamePrefixWithIncrement() {

        var configurableThreadFactory = new ConfigurableThreadFactory(THREAD_NAME);
        var threadOne = configurableThreadFactory.newThread(runnable);
        var threadTwo = configurableThreadFactory.newThread(runnable);

        assertThat(threadOne.getName()).isEqualTo(THREAD_NAME + "-1");
        assertThat(threadTwo.getName()).isEqualTo(THREAD_NAME + "-2");
    }

    @Test
    void verifyNewDaemonThreadHasNamePrefix() {

        var configurableThreadFactory = new ConfigurableThreadFactory(THREAD_NAME, true);
        var thread = configurableThreadFactory.newThread(runnable);

        assertThat(thread.getName()).isEqualTo(THREAD_NAME + "-1");
        assertThat(thread.isDaemon()).isTrue();
    }
}

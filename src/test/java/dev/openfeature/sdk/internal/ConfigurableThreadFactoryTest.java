package dev.openfeature.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ThreadFactory;
import org.junit.jupiter.api.Test;

class ConfigurableThreadFactoryTest {

    private static final String THREAD_NAME = "testthread";
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {}
    };

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
    void verifyNewThreadWithDelegateHasNamePrefix() {

        var threadFactoryMock = mock(ThreadFactory.class);
        var threadMock = mock(Thread.class);
        var configurableThreadFactory = new ConfigurableThreadFactory(THREAD_NAME, threadFactoryMock, false);
        when(threadFactoryMock.newThread(eq(runnable))).thenReturn(threadMock);

        var thread = configurableThreadFactory.newThread(runnable);

        verify(threadMock, times(1)).setName(THREAD_NAME + "-1");
        verify(threadMock, times(1)).setDaemon(false);
    }

    @Test
    void verifyNewDaemonThreadWithDelegateHasNamePrefix() {

        var threadFactoryMock = mock(ThreadFactory.class);
        var threadMock = mock(Thread.class);
        var configurableThreadFactory = new ConfigurableThreadFactory(THREAD_NAME, threadFactoryMock, true);
        when(threadFactoryMock.newThread(eq(runnable))).thenReturn(threadMock);

        var thread = configurableThreadFactory.newThread(runnable);

        verify(threadMock, times(1)).setName(THREAD_NAME + "-1");
        verify(threadMock, times(1)).setDaemon(true);
    }

    @Test
    void verifyNewDaemonThreadHasNamePrefix() {

        var configurableThreadFactory = new ConfigurableThreadFactory(THREAD_NAME, true);
        var thread = configurableThreadFactory.newThread(runnable);

        assertThat(thread.getName()).isEqualTo(THREAD_NAME + "-1");
        assertThat(thread.isDaemon()).isTrue();
    }
}

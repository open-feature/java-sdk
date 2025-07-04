package dev.openfeature.sdk;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated()
class LockingSingeltonTest {

    private static OpenFeatureAPI api;
    private OpenFeatureClient client;
    private AutoCloseableReentrantReadWriteLock apiLock;
    private AutoCloseableReentrantReadWriteLock clientHooksLock;

    @BeforeAll
    static void beforeAll() {
        api = OpenFeatureAPI.getInstance();
        OpenFeatureAPI.getInstance().setProvider("LockingTest", new NoOpProvider());
    }

    @BeforeEach
    void beforeEach() {
        client = (OpenFeatureClient) api.getClient("LockingTest");

        apiLock = setupLock(apiLock, mockInnerReadLock(), mockInnerWriteLock());
        OpenFeatureAPI.lock = apiLock;

        clientHooksLock = setupLock(clientHooksLock, mockInnerReadLock(), mockInnerWriteLock());
    }

    @Nested
    class EventsLocking {

        @Nested
        class Api {

            @Test
            void onShouldWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.on(ProviderEvent.PROVIDER_READY, handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderReadyShouldWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderReady(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderConfigurationChangedShouldWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderConfigurationChanged(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderStaleShouldWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderStale(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderErrorShouldWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderError(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }
        }

        @Nested
        class Client {

            // Note that the API lock is used for adding client handlers, they are all added (indirectly) on the API
            // object.

            @Test
            void onShouldApiWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                client.on(ProviderEvent.PROVIDER_READY, handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderReadyShouldApiWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderReady(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderConfigurationChangedProviderReadyShouldApiWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderConfigurationChanged(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderStaleProviderReadyShouldApiWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderStale(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }

            @Test
            void onProviderErrorProviderReadyShouldApiWriteLockAndUnlock() {
                Consumer handler = mock(Consumer.class);
                api.onProviderError(handler);
                verify(apiLock.writeLock()).lock();
                verify(apiLock.writeLock()).unlock();
            }
        }
    }

    @Test
    void setTransactionalContextPropagatorShouldWriteLockAndUnlock() {
        api.setTransactionContextPropagator(new NoOpTransactionContextPropagator());
        verify(apiLock.writeLock()).lock();
        verify(apiLock.writeLock()).unlock();
    }

    @Test
    void getTransactionalContextPropagatorShouldReadLockAndUnlock() {
        api.getTransactionContextPropagator();
        verify(apiLock.readLock()).lock();
        verify(apiLock.readLock()).unlock();
    }

    private static ReentrantReadWriteLock.ReadLock mockInnerReadLock() {
        ReentrantReadWriteLock.ReadLock readLockMock = mock(ReentrantReadWriteLock.ReadLock.class);
        doNothing().when(readLockMock).lock();
        doNothing().when(readLockMock).unlock();
        return readLockMock;
    }

    private static ReentrantReadWriteLock.WriteLock mockInnerWriteLock() {
        ReentrantReadWriteLock.WriteLock writeLockMock = mock(ReentrantReadWriteLock.WriteLock.class);
        doNothing().when(writeLockMock).lock();
        doNothing().when(writeLockMock).unlock();
        return writeLockMock;
    }

    private AutoCloseableReentrantReadWriteLock setupLock(
            AutoCloseableReentrantReadWriteLock lock,
            AutoCloseableReentrantReadWriteLock.ReadLock readlock,
            AutoCloseableReentrantReadWriteLock.WriteLock writeLock) {
        lock = mock(AutoCloseableReentrantReadWriteLock.class);
        when(lock.readLockAutoCloseable()).thenCallRealMethod();
        when(lock.readLock()).thenReturn(readlock);
        when(lock.writeLockAutoCloseable()).thenCallRealMethod();
        when(lock.writeLock()).thenReturn(writeLock);
        return lock;
    }
}

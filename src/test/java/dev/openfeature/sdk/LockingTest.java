package dev.openfeature.sdk;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;

class LockingTest {

    private static OpenFeatureAPI api;
    private OpenFeatureClient client;
    private AutoCloseableReentrantReadWriteLock apiRwLock;
    private AutoCloseableReentrantReadWriteLock clientRwLock;
    private ReentrantReadWriteLock.ReadLock mockClientReadLock;
    private ReentrantReadWriteLock.WriteLock mockClientWriteLock;
    private ReentrantReadWriteLock.ReadLock mockApiReadLock;
    private ReentrantReadWriteLock.WriteLock mockApiWriteLock;

    @BeforeAll
    static void beforeAll() {
        api = OpenFeatureAPI.getInstance();
    }

    @BeforeEach
    void beforeEach() {
        client = (OpenFeatureClient)api.getClient();

        // mock the inner read and write locks
        mockClientReadLock = mockInnerReadLock();
        mockClientWriteLock = mockInnerWriteLock();
        mockApiReadLock = mockInnerReadLock();
        mockApiWriteLock = mockInnerWriteLock();

        // mock the client rwLock
        clientRwLock = mock(AutoCloseableReentrantReadWriteLock.class);
        when(clientRwLock.readLockAutoCloseable()).thenCallRealMethod();
        when(clientRwLock.readLock()).thenReturn(mockClientReadLock);
        when(clientRwLock.writeLockAutoCloseable()).thenCallRealMethod();
        when(clientRwLock.writeLock()).thenReturn(mockClientWriteLock);
        client.rwLock = clientRwLock;

        // mock the API rwLock
        apiRwLock = mock(AutoCloseableReentrantReadWriteLock.class);
        when(apiRwLock.readLockAutoCloseable()).thenCallRealMethod();
        when(apiRwLock.readLock()).thenReturn(mockApiReadLock);
        when(apiRwLock.writeLockAutoCloseable()).thenCallRealMethod();
        when(apiRwLock.writeLock()).thenReturn(mockApiWriteLock);
        OpenFeatureAPI.rwLock = apiRwLock;
    }

    @Test
    void evaluationShouldReadLockandReadUnlockClientAndApi() {
        client.getBooleanValue("a-key", false);
        verify(mockApiReadLock).lock();
        verify(mockApiReadLock).unlock();
        verify(mockClientReadLock).lock();
        verify(mockClientReadLock).unlock();
    }

    @Test
    void addHooksShouldWriteLockAndUnlock() {
        client.addHooks(new Hook(){});
        verify(mockClientWriteLock).lock();
        verify(mockClientWriteLock).unlock();

        api.addHooks(new Hook(){});
        verify(mockApiWriteLock).lock();
        verify(mockApiWriteLock).unlock();
    }

    @Test
    void setContextShouldWriteLockAndUnlock() {
        client.setEvaluationContext(new MutableContext());
        verify(mockClientWriteLock).lock();
        verify(mockClientWriteLock).unlock();

        api.setEvaluationContext(new MutableContext());
        verify(mockApiWriteLock).lock();
        verify(mockApiWriteLock).unlock();
    }

    @Test
    void setProviderShouldWriteLockAndUnlock() {
        api.setProvider(new DoSomethingProvider());
        verify(mockApiWriteLock).lock();
        verify(mockApiWriteLock).unlock();
    }

    @Test
    void clearHooksShouldWriteLockAndUnlock() {
        api.clearHooks();
        verify(mockApiWriteLock).lock();
        verify(mockApiWriteLock).unlock();
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
}
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
    private AutoCloseableReentrantReadWriteLock apiContextLock;
    private AutoCloseableReentrantReadWriteLock apiHooksLock;
    private AutoCloseableReentrantReadWriteLock apiProviderLock;
    private AutoCloseableReentrantReadWriteLock clientContextLock;
    private AutoCloseableReentrantReadWriteLock clientHooksLock;
    
    @BeforeAll
    static void beforeAll() {
        api = OpenFeatureAPI.getInstance();
    }

    @BeforeEach
    void beforeEach() {
        client = (OpenFeatureClient) api.getClient();
        
        apiContextLock = setupLock(apiContextLock, mockInnerReadLock(), mockInnerWriteLock());
        apiProviderLock = setupLock(apiProviderLock, mockInnerReadLock(), mockInnerWriteLock());
        apiHooksLock = setupLock(apiHooksLock, mockInnerReadLock(), mockInnerWriteLock());
        OpenFeatureAPI.contextLock = apiContextLock;
        OpenFeatureAPI.providerLock = apiProviderLock;
        OpenFeatureAPI.hooksLock = apiHooksLock;

        clientContextLock = setupLock(clientContextLock, mockInnerReadLock(), mockInnerWriteLock());
        clientHooksLock = setupLock(clientHooksLock, mockInnerReadLock(), mockInnerWriteLock());
        client.contextLock = clientContextLock;
        client.hooksLock = clientHooksLock;
    }

    @Test
    void addHooksShouldWriteLockAndUnlock() {
        client.addHooks(new Hook() {
        });
        verify(clientHooksLock.writeLock()).lock();
        verify(clientHooksLock.writeLock()).unlock();

        api.addHooks(new Hook() {
        });
        verify(apiHooksLock.writeLock()).lock();
        verify(apiHooksLock.writeLock()).unlock();
    }

    @Test
    void getHooksShouldReadLockAndUnlock() {
        client.getHooks();
        verify(clientHooksLock.readLock()).lock();
        verify(clientHooksLock.readLock()).unlock();

        api.getHooks();
        verify(apiHooksLock.readLock()).lock();
        verify(apiHooksLock.readLock()).unlock();
    }

    @Test
    void setContextShouldWriteLockAndUnlock() {
        client.setEvaluationContext(new MutableContext());
        verify(clientContextLock.writeLock()).lock();
        verify(clientContextLock.writeLock()).unlock();

        api.setEvaluationContext(new MutableContext());
        verify(apiContextLock.writeLock()).lock();
        verify(apiContextLock.writeLock()).unlock();
    }

    @Test
    void getContextShouldReadLockAndUnlock() {
        client.getEvaluationContext();
        verify(clientContextLock.readLock()).lock();
        verify(clientContextLock.readLock()).unlock();

        api.getEvaluationContext();
        verify(apiContextLock.readLock()).lock();
        verify(apiContextLock.readLock()).unlock();
    }

    @Test
    void setProviderShouldWriteLockAndUnlock() {
        api.setProvider(new DoSomethingProvider());
        verify(apiProviderLock.writeLock()).lock();
        verify(apiProviderLock.writeLock()).unlock();
    }

    @Test
    void clearHooksShouldWriteLockAndUnlock() {
        api.clearHooks();
        verify(apiHooksLock.writeLock()).lock();
        verify(apiHooksLock.writeLock()).unlock();
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

    private AutoCloseableReentrantReadWriteLock setupLock(AutoCloseableReentrantReadWriteLock lock,
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
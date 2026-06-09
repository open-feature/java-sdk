package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;

class GlobalProviderRegistryTest {

    /**
     * Re-registering a provider with the same API instance should not produce a warning.
     * This exercises the {@code existing == this} path in registerGlobalProvider.
     */
    @Test
    @DisplayName("no warning when same API instance re-registers the same provider")
    void noWarningWhenSameInstanceReRegisters() {
        Logger mockLogger = Mockito.mock(Logger.class);
        LoggerMock.setMock(OpenFeatureAPI.class, mockLogger);
        try {
            OpenFeatureAPI api = new OpenFeatureAPI();
            NoOpProvider provider = new NoOpProvider();

            api.registerGlobalProvider(provider);
            api.registerGlobalProvider(provider); // same instance, second call

            Mockito.verify(mockLogger, never()).warn(Mockito.anyString());
        } finally {
            LoggerMock.setMock(OpenFeatureAPI.class, null);
        }
    }

    /**
     * After deregistering a provider, binding it to a different API instance
     * should not produce a warning — proving the entry was removed.
     */
    @Test
    @DisplayName("deregister removes provider from global registry")
    void deregisterRemovesProviderFromRegistry() {
        Logger mockLogger = Mockito.mock(Logger.class);
        LoggerMock.setMock(OpenFeatureAPI.class, mockLogger);
        try {
            OpenFeatureAPI api1 = new OpenFeatureAPI();
            OpenFeatureAPI api2 = new OpenFeatureAPI();
            NoOpProvider provider = new NoOpProvider();

            api1.registerGlobalProvider(provider);
            api1.deregisterGlobalProvider(provider);

            // Should not warn because the provider was deregistered
            api2.registerGlobalProvider(provider);

            Mockito.verify(mockLogger, never()).warn(Mockito.anyString());
        } finally {
            LoggerMock.setMock(OpenFeatureAPI.class, null);
        }
    }

    /**
     * Deregister is a no-op if the calling instance is not the current owner.
     * The original owner's registration should remain intact.
     */
    @Test
    @DisplayName("deregister is a no-op when called by non-owner instance")
    void deregisterIsNoOpForNonOwner() {
        Logger mockLogger = Mockito.mock(Logger.class);
        LoggerMock.setMock(OpenFeatureAPI.class, mockLogger);
        try {
            OpenFeatureAPI api1 = new OpenFeatureAPI();
            OpenFeatureAPI api2 = new OpenFeatureAPI();
            NoOpProvider provider = new NoOpProvider();

            api1.registerGlobalProvider(provider);

            // api2 is not the owner — this should be a no-op
            api2.deregisterGlobalProvider(provider);

            // api2 re-registering should still warn, because api1 still owns it
            api2.registerGlobalProvider(provider);
            Mockito.verify(mockLogger).warn(contains("1.8.4"));
        } finally {
            LoggerMock.setMock(OpenFeatureAPI.class, null);
        }
    }

    /**
     * Calling shutdown() twice on an API instance should be safe (idempotent).
     * The second call returns early because prepareShutdown returns null.
     */
    @Test
    @DisplayName("double shutdown on API instance is safe")
    void doubleShutdownIsSafe() {
        OpenFeatureAPI api = new OpenFeatureAPI();
        api.setProvider(new NoOpProvider());

        assertThatCode(() -> {
                    api.shutdown();
                    api.shutdown();
                })
                .doesNotThrowAnyException();
    }
}

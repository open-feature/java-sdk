package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GlobalProviderRegistryTest {

    /**
     * Re-registering a provider with the same API instance should not throw.
     * This exercises the {@code existing == this} path in registerGlobalProvider.
     */
    @Test
    @DisplayName("no throw when same API instance re-registers the same provider")
    void noThrowWhenSameInstanceReRegisters() {
        OpenFeatureAPI api = new OpenFeatureAPI();
        NoOpProvider provider = new NoOpProvider();

        assertThatCode(() -> {
                    api.registerGlobalProvider(provider);
                    api.registerGlobalProvider(provider); // same instance, second call
                })
                .doesNotThrowAnyException();
    }

    /**
     * After deregistering a provider, binding it to a different API instance
     * should not throw, proving the entry was removed.
     */
    @Test
    @DisplayName("deregister removes provider from global registry")
    void deregisterRemovesProviderFromRegistry() {
        OpenFeatureAPI api1 = new OpenFeatureAPI();
        OpenFeatureAPI api2 = new OpenFeatureAPI();
        NoOpProvider provider = new NoOpProvider();

        api1.registerGlobalProvider(provider);
        api1.deregisterGlobalProvider(provider);

        // should not throw because the provider was deregistered
        assertThatCode(() -> api2.registerGlobalProvider(provider)).doesNotThrowAnyException();
    }

    /**
     * Deregister is a no-op if the calling instance is not the current owner.
     * The original owner's registration should remain intact.
     */
    @Test
    @DisplayName("deregister is a no-op when called by non-owner instance")
    void deregisterIsNoOpForNonOwner() {
        OpenFeatureAPI api1 = new OpenFeatureAPI();
        OpenFeatureAPI api2 = new OpenFeatureAPI();
        NoOpProvider provider = new NoOpProvider();

        api1.registerGlobalProvider(provider);

        // api2 is not the owner; this should be a no-op
        api2.deregisterGlobalProvider(provider);

        // api2 re-registering should still throw, because api1 still owns it
        assertThatThrownBy(() -> api2.registerGlobalProvider(provider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1.8.4");
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

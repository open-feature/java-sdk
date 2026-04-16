package dev.openfeature.sdk.isolated;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.NoOpProvider;
import dev.openfeature.sdk.NoOpTransactionContextPropagator;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.ThreadLocalTransactionContextPropagator;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IsolatedAPITest {

    private final OpenFeatureAPI singleton = OpenFeatureAPI.getInstance();

    @AfterEach
    void restoreSingleton() {
        singleton.shutdown();
        singleton.clearHooks();
        singleton.setEvaluationContext(null);
        singleton.setTransactionContextPropagator(new NoOpTransactionContextPropagator());
    }

    /**
     * Requirement 1.8.1 — factory creates new, distinct instances that
     * conform to the API contract.
     */
    @Test
    @DisplayName("factory creates distinct API instances")
    void factoryCreatesDistinctInstances() {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        assertThat(api1).isInstanceOf(OpenFeatureAPI.class).isNotSameAs(api2);
    }

    /**
     * Requirement 1.8.1 — isolated instances do not share state with
     * the global singleton. Singleton state is restored after the test
     * via {@link #restoreSingleton()}.
     */
    @Test
    @DisplayName("isolated instance does not interfere with singleton")
    void isolatedInstanceDoesNotInterfereWithSingleton() {
        // record singleton baseline
        FeatureProvider singletonProvider = singleton.getProvider();

        OpenFeatureAPI isolated = OpenFeatureAPIFactory.createAPI();
        assertThat(isolated).isNotSameAs(singleton);

        // mutate only the isolated instance
        isolated.setProvider(new InMemoryProvider(Map.of()));
        isolated.addHooks(new NoOpHook());
        isolated.setEvaluationContext(new ImmutableContext("isolated-key"));

        // singleton remains at baseline
        assertThat(singleton.getProvider()).isSameAs(singletonProvider);
        assertThat(singleton.getHooks()).isEmpty();
        assertThat(singleton.getEvaluationContext()).isNull();
    }

    /**
     * Requirement 1.8.1 — providers are isolated between instances.
     */
    @Test
    @DisplayName("providers are isolated between instances")
    void providerIsolation() {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        InMemoryProvider provider = new InMemoryProvider(Map.of());
        api1.setProvider(provider);

        assertThat(api1.getProvider()).isSameAs(provider);
        assertThat(api2.getProvider()).isInstanceOf(NoOpProvider.class);
    }

    /**
     * Requirement 1.8.1 — hooks are isolated between instances.
     */
    @Test
    @DisplayName("hooks are isolated between instances")
    void hookIsolation() {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        api1.addHooks(new NoOpHook());

        assertThat(api1.getHooks()).hasSize(1);
        assertThat(api2.getHooks()).isEmpty();
    }

    /**
     * Requirement 1.8.1 — evaluation context is isolated between instances.
     */
    @Test
    @DisplayName("evaluation context is isolated between instances")
    void evaluationContextIsolation() {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        api1.setEvaluationContext(new ImmutableContext("key-1"));
        api2.setEvaluationContext(new ImmutableContext("key-2"));

        assertThat(api1.getEvaluationContext().getTargetingKey()).isEqualTo("key-1");
        assertThat(api2.getEvaluationContext().getTargetingKey()).isEqualTo("key-2");
    }

    /**
     * Requirement 1.8.1 — event handlers are isolated between instances.
     */
    @Test
    @DisplayName("event handlers are isolated between instances")
    void eventHandlerIsolation() throws Exception {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        AtomicBoolean api1HandlerCalled = new AtomicBoolean(false);
        AtomicBoolean api2HandlerCalled = new AtomicBoolean(false);

        api1.onProviderReady(details -> api1HandlerCalled.set(true));
        api2.onProviderReady(details -> api2HandlerCalled.set(true));

        // setting a provider on api1 should only trigger api1's handler
        api1.setProviderAndWait(new NoOpProvider());

        assertThat(api1HandlerCalled.get()).isTrue();
        assertThat(api2HandlerCalled.get()).isFalse();
    }

    /**
     * Requirement 1.8.1 — transaction context propagators are isolated
     * between instances.
     */
    @Test
    @DisplayName("transaction context propagator is isolated between instances")
    void transactionContextPropagatorIsolation() {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        ThreadLocalTransactionContextPropagator propagator = new ThreadLocalTransactionContextPropagator();
        api1.setTransactionContextPropagator(propagator);

        assertThat(api1.getTransactionContextPropagator()).isSameAs(propagator);
        assertThat(api2.getTransactionContextPropagator()).isInstanceOf(NoOpTransactionContextPropagator.class);
    }

    /**
     * Requirement 1.8.2 — an isolated instance conforms to the same API
     * contract (provider, hooks, context, client creation, flag evaluation).
     */
    @Test
    @DisplayName("isolated instance conforms to API contract")
    void isolatedInstanceConformsToAPIContract() {
        OpenFeatureAPI api = OpenFeatureAPIFactory.createAPI();

        // provider management
        InMemoryProvider provider = new InMemoryProvider(Map.of(
                "flag1", Flag.builder().variant("on", true).variant("off", false).defaultVariant("on").build()));
        api.setProvider(provider);
        assertThat(api.getProvider()).isSameAs(provider);
        assertThat(api.getProviderMetadata()).isNotNull();

        // hooks
        NoOpHook hook = new NoOpHook();
        api.addHooks(hook);
        assertThat(api.getHooks()).containsExactly(hook);

        // context
        api.setEvaluationContext(new ImmutableContext("targeting-key"));
        assertThat(api.getEvaluationContext().getTargetingKey()).isEqualTo("targeting-key");

        // client creation and flag evaluation
        var client = api.getClient("test-domain", "1.0");
        assertThat(client.getMetadata().getDomain()).isEqualTo("test-domain");
        assertThat(client.getBooleanValue("flag1", false)).isTrue();
    }

    /**
     * Requirement 1.8.1 — clearHooks on one instance does not affect another.
     */
    @Test
    @DisplayName("clearHooks does not affect other instances")
    void clearHooksDoesNotAffectOtherInstances() {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        NoOpHook hook = new NoOpHook();
        api1.addHooks(hook);
        api2.addHooks(hook);

        api1.clearHooks();

        assertThat(api1.getHooks()).isEmpty();
        assertThat(api2.getHooks()).hasSize(1);
    }

    /**
     * Requirement 1.8.2 — clients from different isolated instances use
     * their own instance's provider.
     */
    @Test
    @DisplayName("clients use their own instance's provider")
    void clientUsesItsOwnInstanceProvider() {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        api1.setProvider(new InMemoryProvider(Map.of(
                "flag1", Flag.builder().variant("on", true).variant("off", false).defaultVariant("on").build())));

        var client1 = api1.getClient();
        var client2 = api2.getClient();

        assertThat(client1.getBooleanValue("flag1", false)).isTrue();
        // api2 has NoOpProvider, so it returns the default
        assertThat(client2.getBooleanValue("flag1", false)).isFalse();
    }
}

package dev.openfeature.sdk.isolated;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.NoOpProvider;
import dev.openfeature.sdk.NoOpTransactionContextPropagator;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.OpenFeatureAPIFactory;
import dev.openfeature.sdk.ThreadLocalTransactionContextPropagator;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class IsolatedAPITest {

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
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("event handlers are isolated between instances")
    void eventHandlerIsolation() throws Exception {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        CountDownLatch api1HandlerLatch = new CountDownLatch(1);
        AtomicBoolean api2HandlerCalled = new AtomicBoolean(false);

        // Handlers are dispatched asynchronously; use a latch to await api1's handler.
        api1.onProviderReady(details -> api1HandlerLatch.countDown());
        api2.onProviderReady(details -> api2HandlerCalled.set(true));

        // setting a provider on api1 should only trigger api1's handler
        api1.setProviderAndWait(new NoOpProvider());

        assertThat(api1HandlerLatch.await(1, TimeUnit.SECONDS)).isTrue();
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
    void isolatedInstanceConformsToAPIContract() throws Exception {
        OpenFeatureAPI api = OpenFeatureAPIFactory.createAPI();

        // provider management
        InMemoryProvider provider = new InMemoryProvider(Map.of(
                "flag1",
                Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("on")
                        .build()));
        api.setProviderAndWait(provider);
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
    void clientUsesItsOwnInstanceProvider() throws Exception {
        OpenFeatureAPI api1 = OpenFeatureAPIFactory.createAPI();
        OpenFeatureAPI api2 = OpenFeatureAPIFactory.createAPI();

        api1.setProviderAndWait(new InMemoryProvider(Map.of(
                "flag1",
                Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("on")
                        .build())));

        var client1 = api1.getClient();
        var client2 = api2.getClient();

        assertThat(client1.getBooleanValue("flag1", false)).isTrue();
        // api2 has NoOpProvider, so it returns the default
        assertThat(client2.getBooleanValue("flag1", false)).isFalse();
    }
}

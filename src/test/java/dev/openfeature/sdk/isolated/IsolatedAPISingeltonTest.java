package dev.openfeature.sdk.isolated;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.NoOpTransactionContextPropagator;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.ThreadLocalTransactionContextPropagator;
import dev.openfeature.sdk.isolated.OpenFeatureAPIFactory;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IsolatedAPISingeltonTest {

    private final OpenFeatureAPI singleton = OpenFeatureAPI.getInstance();

    @AfterEach
    void restoreSingleton() {
        singleton.shutdown();
        singleton.clearHooks();
        singleton.setEvaluationContext(null);
        singleton.setTransactionContextPropagator(new NoOpTransactionContextPropagator());
    }

    /**
     * Requirement 1.8.1 — isolated instances do not share state with
     * the global singleton.
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
     * Requirement 1.8.1 — mutating the singleton does not affect a
     * previously created isolated instance.
     */
    @Test
    @DisplayName("singleton does not interfere with isolated instance")
    void singletonDoesNotInterfereWithIsolatedInstance() {
        OpenFeatureAPI isolated = OpenFeatureAPIFactory.createAPI();

        // record isolated baseline
        FeatureProvider isolatedProvider = isolated.getProvider();

        // mutate only the singleton
        singleton.setProvider(new InMemoryProvider(Map.of()));
        singleton.addHooks(new NoOpHook());
        singleton.setEvaluationContext(new ImmutableContext("singleton-key"));
        singleton.setTransactionContextPropagator(new ThreadLocalTransactionContextPropagator());

        // isolated instance remains at baseline
        assertThat(isolated.getProvider()).isSameAs(isolatedProvider);
        assertThat(isolated.getHooks()).isEmpty();
        assertThat(isolated.getEvaluationContext()).isNull();
        assertThat(isolated.getTransactionContextPropagator()).isInstanceOf(NoOpTransactionContextPropagator.class);
    }
}

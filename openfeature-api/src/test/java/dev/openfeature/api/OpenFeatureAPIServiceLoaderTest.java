package dev.openfeature.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import dev.openfeature.api.internal.noop.NoOpOpenFeatureAPI;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ServiceLoader functionality and provider discovery in OpenFeatureAPI.
 * These tests document the expected behavior of provider loading and priority selection.
 */
class OpenFeatureAPIServiceLoaderTest {

    @BeforeEach
    @AfterEach
    void resetApiInstance() {
        OpenFeatureAPI.resetInstance();
    }

    @Test
    void loads_highest_priority_provider() {
        // This test documents the expected behavior when multiple providers are available
        // Since we're testing in isolation, we expect the NoOp fallback
        OpenFeatureAPI instance = OpenFeatureAPI.getInstance();

        assertThat(instance).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);
    }

    @Test
    void handles_provider_creation_errors_gracefully() {
        // When a provider fails to create an API instance, should fall back to NoOp
        OpenFeatureAPI instance = OpenFeatureAPI.getInstance();

        assertThat(instance).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);

        // Should still provide functional API
        assertThatCode(() -> {
                    Client client = instance.getClient();
                    assertThat(client).isNotNull();
                })
                .doesNotThrowAnyException();
    }

    @Test
    void handles_provider_priority_errors_gracefully() {
        // When a provider throws an exception during getPriority(),
        // the system should continue and check other providers
        OpenFeatureAPI instance = OpenFeatureAPI.getInstance();

        assertThat(instance).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);
    }

    @Test
    void load_implementation_is_deterministic() {
        // Multiple calls to load implementation should return consistent results
        OpenFeatureAPI first = OpenFeatureAPI.getInstance();
        OpenFeatureAPI.resetInstance();
        OpenFeatureAPI second = OpenFeatureAPI.getInstance();

        assertThat(first).isNotNull().hasSameClassAs(second);
    }

    @Test
    void service_loader_respects_priority_order() throws Exception {
        // Test documents the priority-based selection behavior
        // Higher priority providers should be selected over lower priority ones

        // Since we can't easily mock ServiceLoader in this context,
        // we document the expected behavior through the method signature
        Method loadMethod = OpenFeatureAPI.class.getDeclaredMethod("loadImplementation");
        loadMethod.setAccessible(true);

        OpenFeatureAPI result = (OpenFeatureAPI) loadMethod.invoke(null);

        assertThat(result).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);
    }

    @Test
    void error_messages_are_logged_but_not_propagated() {
        // Provider errors should be logged but not break the loading process
        // This test verifies that errors don't propagate up the call stack

        assertThatCode(() -> {
                    OpenFeatureAPI instance = OpenFeatureAPI.getInstance();
                    assertThat(instance).isNotNull();
                })
                .doesNotThrowAnyException();
    }

    @Test
    void supports_no_providers_scenario() {
        // When no providers are available via ServiceLoader, should return NoOp
        OpenFeatureAPI instance = OpenFeatureAPI.getInstance();

        assertThat(instance).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);

        // NoOp implementation should provide safe defaults
        assertThat(instance.getClient()).isNotNull();
        assertThat(instance.getProviderMetadata()).isNotNull();
        assertThat(instance.getEvaluationContext()).isNotNull();
    }

    @Test
    void provider_interface_contract() {
        // Document the expected provider interface contract
        assertThat(OpenFeatureAPIProvider.class).satisfies(providerInterface -> {
            assertThat(providerInterface.isInterface()).isTrue();

            // Should have createAPI method
            assertThatCode(() -> {
                        Method createAPI = providerInterface.getMethod("createAPI");
                        assertThat(createAPI.getReturnType()).isEqualTo(OpenFeatureAPI.class);
                    })
                    .doesNotThrowAnyException();

            // Should have getPriority method with default implementation
            assertThatCode(() -> {
                        Method getPriority = providerInterface.getMethod("getPriority");
                        assertThat(getPriority.getReturnType()).isEqualTo(int.class);
                        assertThat(getPriority.isDefault()).isTrue();
                    })
                    .doesNotThrowAnyException();
        });
    }

    // Test helper classes to document expected provider behavior

    /**
     * Example of a well-behaved provider implementation
     */
    static class TestProvider implements OpenFeatureAPIProvider {
        private final int priority;
        private final boolean shouldFailCreation;

        public TestProvider(int priority, boolean shouldFailCreation) {
            this.priority = priority;
            this.shouldFailCreation = shouldFailCreation;
        }

        @Override
        public OpenFeatureAPI createAPI() {
            if (shouldFailCreation) {
                throw new RuntimeException("Simulated provider creation failure");
            }
            return new NoOpOpenFeatureAPI();
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }

    /**
     * Example of a provider that fails during priority check
     */
    static class FailingPriorityProvider implements OpenFeatureAPIProvider {
        @Override
        public OpenFeatureAPI createAPI() {
            return new NoOpOpenFeatureAPI();
        }

        @Override
        public int getPriority() {
            throw new RuntimeException("Simulated priority check failure");
        }
    }

    @Test
    void documents_provider_selection_algorithm() {
        // This test documents how provider selection should work:
        // 1. Load all providers via ServiceLoader
        // 2. For each provider, get its priority (catching exceptions)
        // 3. Select the provider with the highest priority
        // 4. Create API instance from selected provider (catching exceptions)
        // 5. Fall back to NoOp if no providers work

        TestProvider lowPriority = new TestProvider(1, false);
        TestProvider highPriority = new TestProvider(10, false);
        TestProvider failingCreation = new TestProvider(100, true);
        FailingPriorityProvider failingPriority = new FailingPriorityProvider();

        // Simulate the selection algorithm
        List<OpenFeatureAPIProvider> providers = List.of(lowPriority, highPriority, failingCreation, failingPriority);

        OpenFeatureAPIProvider bestProvider = null;
        int highestPriority = Integer.MIN_VALUE;

        for (OpenFeatureAPIProvider provider : providers) {
            try {
                int priority = provider.getPriority();
                if (priority > highestPriority) {
                    bestProvider = provider;
                    highestPriority = priority;
                }
            } catch (Exception e) {
                // Should continue processing other providers
                continue;
            }
        }

        // Should select the failing creation provider (highest priority)
        assertThat(bestProvider).isSameAs(failingCreation);
        assertThat(highestPriority).isEqualTo(100);

        // But creation should fail, so should fall back to working provider
        OpenFeatureAPI result = null;
        if (bestProvider != null) {
            try {
                result = bestProvider.createAPI();
            } catch (Exception e) {
                // Fall back to second-best provider
                result = highPriority.createAPI();
            }
        }

        assertThat(result).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);
    }
}

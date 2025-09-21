package dev.openfeature.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.EvaluationContextHolder;
import dev.openfeature.api.events.EventBus;
import dev.openfeature.api.internal.noop.NoOpOpenFeatureAPI;
import dev.openfeature.api.lifecycle.Hookable;
import dev.openfeature.api.lifecycle.Lifecycle;
import dev.openfeature.api.types.ProviderMetadata;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class OpenFeatureAPITest {

    @BeforeEach
    @AfterEach
    void resetApiInstance() {
        // Reset the singleton instance before and after each test
        OpenFeatureAPI.resetInstance();
    }

    @Specification(
            number = "1.1.1",
            text =
                    "The API, and any state it maintains SHOULD exist as a global singleton, even in cases wherein multiple versions of the API are present at runtime.")
    @Test
    void singleton_pattern_returns_same_instance() {
        OpenFeatureAPI firstInstance = OpenFeatureAPI.getInstance();
        OpenFeatureAPI secondInstance = OpenFeatureAPI.getInstance();

        assertThat(firstInstance).isNotNull().isSameAs(secondInstance);
    }

    @Test
    void singleton_uses_double_checked_locking() throws Exception {
        // Verify the class implements proper double-checked locking pattern
        Field instanceField = OpenFeatureAPI.class.getDeclaredField("instance");
        Field lockField = OpenFeatureAPI.class.getDeclaredField("instanceLock");

        assertThat(instanceField).satisfies(field -> {
            assertThat(Modifier.isStatic(field.getModifiers())).isTrue();
            assertThat(Modifier.isVolatile(field.getModifiers())).isTrue();
        });

        assertThat(lockField).satisfies(field -> {
            assertThat(Modifier.isStatic(field.getModifiers())).isTrue();
            assertThat(Modifier.isFinal(field.getModifiers())).isTrue();
        });
    }

    @Specification(
            number = "1.1.1",
            text =
                    "The API, and any state it maintains SHOULD exist as a global singleton, even in cases wherein multiple versions of the API are present at runtime.")
    @Test
    @Timeout(10)
    void singleton_is_thread_safe() throws Exception {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        // Array to store instances from each thread
        OpenFeatureAPI[] instances = new OpenFeatureAPI[threadCount];

        // Start multiple threads simultaneously
        IntStream.range(0, threadCount).forEach(i -> {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    instances[i] = OpenFeatureAPI.getInstance();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        });

        // Release all threads at once
        startLatch.countDown();

        // Wait for all threads to complete
        assertThat(finishLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        // Verify all threads got the same instance
        OpenFeatureAPI expectedInstance = instances[0];
        assertThat(instances).isNotNull().allSatisfy(instance -> assertThat(instance)
                .isSameAs(expectedInstance));
    }

    @Test
    void falls_back_to_noop_when_no_providers_available() {
        // When no ServiceLoader providers are available, should return NoOpOpenFeatureAPI
        OpenFeatureAPI instance = OpenFeatureAPI.getInstance();

        assertThat(instance).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);
    }

    @Test
    void reset_instance_clears_singleton() {
        OpenFeatureAPI firstInstance = OpenFeatureAPI.getInstance();

        OpenFeatureAPI.resetInstance();

        OpenFeatureAPI secondInstance = OpenFeatureAPI.getInstance();

        assertThat(firstInstance).isNotNull().isNotSameAs(secondInstance);

        assertThat(secondInstance).isNotNull();
    }

    @Test
    void reset_instance_is_thread_safe() throws Exception {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // First, get an instance
        OpenFeatureAPI initialInstance = OpenFeatureAPI.getInstance();
        assertThat(initialInstance).isNotNull();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        // Have multiple threads reset and get instances simultaneously
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];

        IntStream.range(0, threadCount).forEach(i -> {
            futures[i] = CompletableFuture.runAsync(
                    () -> {
                        try {
                            startLatch.await();
                            if (i % 2 == 0) {
                                OpenFeatureAPI.resetInstance();
                            } else {
                                OpenFeatureAPI.getInstance();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            finishLatch.countDown();
                        }
                    },
                    executor);
        });

        startLatch.countDown();
        assertThat(finishLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Should not throw any exceptions
        assertThatCode(() -> CompletableFuture.allOf(futures).join()).doesNotThrowAnyException();

        // Should still be able to get a valid instance
        OpenFeatureAPI finalInstance = OpenFeatureAPI.getInstance();
        assertThat(finalInstance).isNotNull();

        executor.shutdown();
    }

    @Test
    void api_implements_all_required_interfaces() {
        OpenFeatureAPI instance = OpenFeatureAPI.getInstance();

        assertThat(instance)
                .isInstanceOf(OpenFeatureCore.class)
                .isInstanceOf(Hookable.class)
                .isInstanceOf(EvaluationContextHolder.class)
                .isInstanceOf(EventBus.class)
                .isInstanceOf(Transactional.class)
                .isInstanceOf(Lifecycle.class);
    }

    @Test
    void class_is_abstract() {
        assertThat(Modifier.isAbstract(OpenFeatureAPI.class.getModifiers())).isTrue();
    }

    @Test
    void load_implementation_method_is_private() throws Exception {
        Method loadImplementationMethod = OpenFeatureAPI.class.getDeclaredMethod("loadImplementation");

        assertThat(loadImplementationMethod).satisfies(method -> {
            assertThat(Modifier.isPrivate(method.getModifiers())).isTrue();
            assertThat(Modifier.isStatic(method.getModifiers())).isTrue();
            assertThat(method.getReturnType()).isEqualTo(OpenFeatureAPI.class);
        });
    }

    @Test
    void reset_instance_method_is_protected() throws Exception {
        Method resetInstanceMethod = OpenFeatureAPI.class.getDeclaredMethod("resetInstance");

        assertThat(resetInstanceMethod).satisfies(method -> {
            assertThat(Modifier.isProtected(method.getModifiers())).isTrue();
            assertThat(Modifier.isStatic(method.getModifiers())).isTrue();
            assertThat(method.getReturnType()).isEqualTo(void.class);
        });
    }

    @Test
    void consecutive_calls_return_same_instance_without_synchronization() {
        // Test that after initialization, getInstance() returns the same instance
        // without needing synchronization (should be fast)
        OpenFeatureAPI firstCall = OpenFeatureAPI.getInstance();

        // These subsequent calls should be very fast (no synchronization needed)
        OpenFeatureAPI secondCall = OpenFeatureAPI.getInstance();
        OpenFeatureAPI thirdCall = OpenFeatureAPI.getInstance();

        assertThat(firstCall).isSameAs(secondCall).isSameAs(thirdCall);
    }

    @Specification(
            number = "1.1.6",
            text =
                    "The API MUST provide a function for creating a client which accepts the following options: domain (optional).")
    @Specification(
            number = "1.1.7",
            text = "The client creation function MUST NOT throw, or otherwise abnormally terminate.")
    @Test
    void api_provides_core_functionality() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();

        // Verify the API provides basic client functionality
        assertThatCode(() -> {
                    Client client = api.getClient();
                    assertThat(client).isNotNull();
                })
                .doesNotThrowAnyException();

        // Verify the API provides basic provider functionality
        assertThatCode(() -> {
                    ProviderMetadata metadata = api.getProviderMetadata();
                    assertThat(metadata).isNotNull();
                })
                .doesNotThrowAnyException();
    }

    @Test
    void api_handles_errors_gracefully() {
        // The API should handle various error conditions gracefully
        // This is primarily tested through the ServiceLoader error handling

        OpenFeatureAPI instance = OpenFeatureAPI.getInstance();

        assertThat(instance).isNotNull().isInstanceOf(NoOpOpenFeatureAPI.class);

        // Even the no-op implementation should provide working functionality
        assertThatCode(() -> {
                    Client client = instance.getClient();
                    assertThat(client).isNotNull();

                    // Should be able to make evaluations without errors
                    boolean result = client.getBooleanValue("test-flag", false);
                    assertThat(result).isFalse(); // Default value
                })
                .doesNotThrowAnyException();
    }

    @Test
    void instance_field_visibility() throws Exception {
        Field instanceField = OpenFeatureAPI.class.getDeclaredField("instance");
        Field lockField = OpenFeatureAPI.class.getDeclaredField("instanceLock");

        // Verify proper encapsulation
        assertThat(instanceField.canAccess(null)).isFalse(); // private field
        assertThat(lockField.canAccess(null)).isFalse(); // private field
    }

    @Test
    void memory_consistency_with_volatile() throws Exception {
        // This test documents the importance of the volatile keyword
        Field instanceField = OpenFeatureAPI.class.getDeclaredField("instance");

        assertThat(Modifier.isVolatile(instanceField.getModifiers()))
                .as("Instance field must be volatile for memory consistency in double-checked locking")
                .isTrue();
    }

    @Specification(
            number = "1.1.5",
            text = "The API MUST provide a function for retrieving the metadata field of the configured provider.")
    @Specification(
            number = "1.1.6",
            text =
                    "The API MUST provide a function for creating a client which accepts the following options: domain (optional).")
    @Specification(
            number = "1.1.7",
            text = "The client creation function MUST NOT throw, or otherwise abnormally terminate.")
    @Test
    void supports_multiple_interface_implementations() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();

        // Verify it can be used as different interface types
        assertThat(api).isInstanceOf(OpenFeatureCore.class);

        assertThatCode(() -> {
                    Client client = api.getClient();
                    assertThat(client).isNotNull();
                })
                .doesNotThrowAnyException();

        assertThatCode(((Hookable<?>) api)::clearHooks).doesNotThrowAnyException();

        assertThatCode(() -> {
                    EvaluationContext context = api.getEvaluationContext();
                    assertThat(context).isNotNull();
                })
                .doesNotThrowAnyException();
    }
}

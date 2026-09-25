package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.exceptions.TypeMismatchError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MultiProviderHookExecutorTest {

    private final MultiProviderHookExecutor executor = new MultiProviderHookExecutor();
    private final ClientMetadata clientMetadata = () -> "test";

    @Test
    void shortCircuitsDirectlyWhenProviderHasNoHooks() {
        AtomicBoolean called = new AtomicBoolean(false);
        ProviderEvaluation<String> result = executor.execute(
                stubProvider("p", Collections.emptyList()),
                "flag",
                "default",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> {
                    called.set(true);
                    return ProviderEvaluation.<String>builder().value("direct").build();
                });

        assertTrue(called.get());
        assertEquals("direct", result.getValue());
    }

    @Test
    void shortCircuitsWhenProviderReturnsNullHookList() {
        AtomicBoolean called = new AtomicBoolean(false);
        ProviderEvaluation<String> result = executor.execute(
                stubProvider("p", null),
                "flag",
                "default",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> {
                    called.set(true);
                    return ProviderEvaluation.<String>builder().value("direct").build();
                });

        assertTrue(called.get());
        assertEquals("direct", result.getValue());
    }

    @Test
    void runsHooksInSameOrderAsStandaloneEvaluation() {
        // SDK order (see HookSupport): before reversed, after/finally in registration order
        List<String> calls = new ArrayList<>();
        Hook<String> first = orderRecordingHook(calls, "first");
        Hook<String> second = orderRecordingHook(calls, "second");

        executor.execute(
                stubProvider("p", List.of(first, second)),
                "flag",
                "default",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> ProviderEvaluation.<String>builder().value("ok").build());

        assertEquals(
                List.of(
                        "before:second",
                        "before:first",
                        "after:first",
                        "after:second",
                        "finally:first",
                        "finally:second"),
                calls);
    }

    @Test
    void shortCircuitsWhenNoHooksSupportTheFlagType() {
        AtomicBoolean called = new AtomicBoolean(false);
        Hook<Boolean> boolOnlyHook = new Hook<Boolean>() {
            @Override
            public boolean supportsFlagValueType(FlagValueType type) {
                return type == FlagValueType.BOOLEAN;
            }
        };
        ProviderEvaluation<String> result = executor.execute(
                stubProvider("p", List.of(boolOnlyHook)),
                "flag",
                "default",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> {
                    called.set(true);
                    return ProviderEvaluation.<String>builder().value("direct").build();
                });

        assertTrue(called.get());
        assertEquals("direct", result.getValue());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void toleratesNullReturnedFromBeforeHook() {
        Hook nullBeforeHook = new Hook() {
            @Override
            public Optional before(HookContext ctx, Map hints) {
                return null;
            }
        };
        ProviderEvaluation<String> result = executor.execute(
                stubProvider("p", List.of(nullBeforeHook)),
                "flag",
                "default",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> ProviderEvaluation.<String>builder().value("ok").build());

        assertEquals("ok", result.getValue());
    }

    @Test
    void swallowsExceptionThrownFromErrorHook() {
        AtomicBoolean errorHookCalled = new AtomicBoolean(false);
        Hook<String> throwingErrorHook = new Hook<String>() {
            @Override
            public void error(HookContext<String> ctx, Exception error, Map<String, Object> hints) {
                errorHookCalled.set(true);
                throw new RuntimeException("error hook exploded");
            }
        };
        RuntimeException providerEx = new RuntimeException("provider failed");

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> executor.execute(
                        stubProvider("p", List.of(throwingErrorHook)),
                        "flag",
                        "default",
                        FlagValueType.STRING,
                        null,
                        clientMetadata,
                        null,
                        (p, ctx) -> {
                            throw providerEx;
                        }));

        assertTrue(errorHookCalled.get(), "error() hook should have been called");
        assertEquals(providerEx, thrown, "original provider exception must propagate");
    }

    @Test
    void swallowsExceptionThrownFromFinallyAfterHook() {
        Hook<String> throwingFinallyHook = new Hook<String>() {
            @Override
            public void finallyAfter(
                    HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
                throw new RuntimeException("finallyAfter exploded");
            }
        };

        assertDoesNotThrow(() -> executor.execute(
                stubProvider("p", List.of(throwingFinallyHook)),
                "flag",
                "default",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> ProviderEvaluation.<String>builder().value("ok").build()));
    }

    @Test
    void finallyAfterReceivesSyntheticDetailsWhenBeforeThrows() {
        AtomicReference<FlagEvaluationDetails<String>> captured = new AtomicReference<>();
        Hook<String> hook = new Hook<String>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
                throw new RuntimeException("before failed");
            }

            @Override
            public void finallyAfter(
                    HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
                captured.set(details);
            }
        };

        assertThrows(
                RuntimeException.class,
                () -> executor.execute(
                        stubProvider("p", List.of(hook)),
                        "flag",
                        "fallback",
                        FlagValueType.STRING,
                        null,
                        clientMetadata,
                        null,
                        (p, ctx) ->
                                ProviderEvaluation.<String>builder().value("ok").build()));

        assertNotNull(captured.get(), "finallyAfter must be called even when before() throws");
        assertEquals("flag", captured.get().getFlagKey());
        assertEquals("fallback", captured.get().getValue());
    }

    @Test
    void passesDefaultValueThroughToHookContext() {
        AtomicReference<Object> capturedDefault = new AtomicReference<>();
        Hook<String> hook = new Hook<String>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
                capturedDefault.set(ctx.getDefaultValue());
                return Optional.empty();
            }
        };

        executor.execute(
                stubProvider("p", List.of(hook)),
                "flag",
                "the-default",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> ProviderEvaluation.<String>builder().value("ok").build());

        assertEquals("the-default", capturedDefault.get());
    }

    @Test
    void runsErrorStageWhenProviderReturnsAnErrorCodedEvaluation() {
        AtomicReference<Exception> capturedError = new AtomicReference<>();
        AtomicReference<FlagEvaluationDetails<String>> capturedDetails = new AtomicReference<>();
        AtomicBoolean afterCalled = new AtomicBoolean(false);
        Hook<String> hook = new Hook<String>() {
            @Override
            public void after(
                    HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
                afterCalled.set(true);
            }

            @Override
            public void error(HookContext<String> ctx, Exception error, Map<String, Object> hints) {
                capturedError.set(error);
            }

            @Override
            public void finallyAfter(
                    HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
                capturedDetails.set(details);
            }
        };

        ProviderEvaluation<String> result = executor.execute(
                stubProvider("p", List.of(hook)),
                "flag",
                "fallback",
                FlagValueType.STRING,
                null,
                clientMetadata,
                null,
                (p, ctx) -> ProviderEvaluation.<String>builder()
                        .errorCode(ErrorCode.FLAG_NOT_FOUND)
                        .errorMessage("nope")
                        .build());

        assertEquals(ErrorCode.FLAG_NOT_FOUND, result.getErrorCode());
        assertFalse(afterCalled.get(), "after() must not run for an error-coded evaluation");
        assertInstanceOf(FlagNotFoundError.class, capturedError.get());
        assertEquals("fallback", capturedDetails.get().getValue());
        assertEquals(Reason.ERROR.toString(), capturedDetails.get().getReason());
    }

    @Test
    void usesErrorCodeFromOpenFeatureErrorThrownByProvider() {
        AtomicReference<FlagEvaluationDetails<String>> capturedDetails = new AtomicReference<>();
        Hook<String> hook = new Hook<String>() {
            @Override
            public void finallyAfter(
                    HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
                capturedDetails.set(details);
            }
        };

        assertThrows(
                TypeMismatchError.class,
                () -> executor.execute(
                        stubProvider("p", List.of(hook)),
                        "flag",
                        "fallback",
                        FlagValueType.STRING,
                        null,
                        clientMetadata,
                        null,
                        (p, ctx) -> {
                            throw new TypeMismatchError("wrong type");
                        }));

        assertEquals(ErrorCode.TYPE_MISMATCH, capturedDetails.get().getErrorCode());
        assertEquals("wrong type", capturedDetails.get().getErrorMessage());
        assertEquals("fallback", capturedDetails.get().getValue());
    }

    @Test
    void passesClientMetadataAndHintsToHooks() {
        AtomicReference<String> capturedClientName = new AtomicReference<>();
        AtomicReference<Map<String, Object>> capturedHints = new AtomicReference<>();
        Hook<String> hook = new Hook<String>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
                capturedClientName.set(ctx.getClientMetadata().getName());
                capturedHints.set(hints);
                return Optional.empty();
            }
        };

        executor.execute(
                stubProvider("p", List.of(hook)),
                "flag",
                "default",
                FlagValueType.STRING,
                null,
                () -> "my-client",
                Map.of("hint", "value"),
                (p, ctx) -> ProviderEvaluation.<String>builder().value("ok").build());

        assertEquals("my-client", capturedClientName.get());
        assertEquals("value", capturedHints.get().get("hint"));
    }

    @Test
    void mergesContextReturnedFromBeforeHookAndPreservesTargetingKey() {
        AtomicReference<EvaluationContext> capturedProviderContext = new AtomicReference<>();
        Hook<String> hook = new Hook<String>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
                return Optional.of(new ImmutableContext(Map.of("added", new Value("yes"))));
            }
        };

        executor.execute(
                stubProvider("p", List.of(hook)),
                "flag",
                "default",
                FlagValueType.STRING,
                new ImmutableContext("user-1", Map.of("original", new Value("kept"))),
                clientMetadata,
                null,
                (p, ctx) -> {
                    capturedProviderContext.set(ctx);
                    return ProviderEvaluation.<String>builder().value("ok").build();
                });

        EvaluationContext providerContext = capturedProviderContext.get();
        assertNotNull(providerContext);
        assertEquals("user-1", providerContext.getTargetingKey());
        assertEquals("kept", providerContext.getValue("original").asString());
        assertEquals("yes", providerContext.getValue("added").asString());
    }

    @Test
    void passesContextWithoutTargetingKey() {
        AtomicReference<EvaluationContext> capturedProviderContext = new AtomicReference<>();
        Hook<String> hook = new Hook<String>() {};

        executor.execute(
                stubProvider("p", List.of(hook)),
                "flag",
                "default",
                FlagValueType.STRING,
                new ImmutableContext(Map.of("original", new Value("kept"))),
                clientMetadata,
                null,
                (p, ctx) -> {
                    capturedProviderContext.set(ctx);
                    return ProviderEvaluation.<String>builder().value("ok").build();
                });

        assertNull(capturedProviderContext.get().getTargetingKey());
        assertEquals("kept", capturedProviderContext.get().getValue("original").asString());
    }

    private static Hook<String> orderRecordingHook(List<String> calls, String name) {
        return new Hook<String>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
                calls.add("before:" + name);
                return Optional.empty();
            }

            @Override
            public void after(
                    HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
                calls.add("after:" + name);
            }

            @Override
            public void finallyAfter(
                    HookContext<String> ctx, FlagEvaluationDetails<String> details, Map<String, Object> hints) {
                calls.add("finally:" + name);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private static FeatureProvider stubProvider(String name, List<Hook> hooks) {
        return new EventProvider() {
            @Override
            public Metadata getMetadata() {
                return () -> name;
            }

            @Override
            public List<Hook> getProviderHooks() {
                return hooks;
            }

            @Override
            public ProviderEvaluation<Boolean> getBooleanEvaluation(
                    String key, Boolean defaultValue, EvaluationContext ctx) {
                return ProviderEvaluation.<Boolean>builder().value(defaultValue).build();
            }

            @Override
            public ProviderEvaluation<String> getStringEvaluation(
                    String key, String defaultValue, EvaluationContext ctx) {
                return ProviderEvaluation.<String>builder().value(defaultValue).build();
            }

            @Override
            public ProviderEvaluation<Integer> getIntegerEvaluation(
                    String key, Integer defaultValue, EvaluationContext ctx) {
                return ProviderEvaluation.<Integer>builder().value(defaultValue).build();
            }

            @Override
            public ProviderEvaluation<Double> getDoubleEvaluation(
                    String key, Double defaultValue, EvaluationContext ctx) {
                return ProviderEvaluation.<Double>builder().value(defaultValue).build();
            }

            @Override
            public ProviderEvaluation<Value> getObjectEvaluation(
                    String key, Value defaultValue, EvaluationContext ctx) {
                return ProviderEvaluation.<Value>builder().value(defaultValue).build();
            }
        };
    }
}

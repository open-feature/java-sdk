package dev.openfeature.sdk.multiprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.FlagEvaluationOptions;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Reason;
import dev.openfeature.sdk.Value;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MultiProviderHooksTest {

    @Test
    void shouldExecuteProviderHooksAndKeepPerProviderContextIsolation() throws Exception {
        RecordingHook firstHook = new RecordingHook("provider1");
        RecordingHook secondHook = new RecordingHook("provider2");

        HookedStringProvider provider1 = new HookedStringProvider(
                "provider1",
                List.of(firstHook),
                ProviderEvaluation.<String>builder()
                        .errorCode(dev.openfeature.sdk.ErrorCode.GENERAL)
                        .errorMessage("failed")
                        .build());
        HookedStringProvider provider2 = new HookedStringProvider(
                "provider2",
                List.of(secondHook),
                ProviderEvaluation.<String>builder().value("ok").build());

        MultiProvider multiProvider = new MultiProvider(List.of(provider1, provider2), new FirstSuccessfulStrategy());
        multiProvider.initialize(null);

        ProviderEvaluation<String> evaluation = multiProvider.getStringEvaluation("flag", "default", null);

        assertEquals("ok", evaluation.getValue());

        assertEquals(1, firstHook.beforeCount.get());
        assertEquals(0, firstHook.afterCount.get());
        assertEquals(1, firstHook.errorCount.get());
        assertEquals(1, firstHook.finallyCount.get());

        assertEquals(1, secondHook.beforeCount.get());
        assertEquals(1, secondHook.afterCount.get());
        assertEquals(0, secondHook.errorCount.get());
        assertEquals(1, secondHook.finallyCount.get());

        assertEquals("provider1", provider1.lastEvaluationContext.getValue("hookOwner").asString());
        assertNull(provider1.lastEvaluationContext.getValue("provider2Marker"));
        assertNotNull(firstHook.lastFinallyDetails);
        assertEquals(ErrorCode.GENERAL, firstHook.lastFinallyDetails.getErrorCode());
        assertEquals(Reason.ERROR.toString(), firstHook.lastFinallyDetails.getReason());
        assertEquals("default", firstHook.lastFinallyDetails.getValue());
        assertEquals("failed", firstHook.lastFinallyDetails.getErrorMessage());

        assertEquals("provider2", provider2.lastEvaluationContext.getValue("hookOwner").asString());
        assertNull(provider2.lastEvaluationContext.getValue("provider1Marker"));
    }

    @Test
    void shouldExposeContextCapturingProviderHook() throws Exception {
        HookedStringProvider provider1 = new HookedStringProvider(
                "provider1",
                List.of(),
                ProviderEvaluation.<String>builder().value("ok").build());

        MultiProvider multiProvider = new MultiProvider(
                List.of(provider1), new FirstSuccessfulStrategy());
        multiProvider.initialize(null);

        // MultiProvider should expose a provider hook for context capture
        var providerHooks = multiProvider.getProviderHooks();
        assertNotNull(providerHooks);
        assertEquals(1, providerHooks.size(), "Should have exactly one context-capturing hook");
    }

    @Test
    void shouldPassHookHintsAndClientMetadataAndEnrichThrownProviderErrors() throws Exception {
        RecordingHook firstHook = new RecordingHook("provider1");
        RecordingHook secondHook = new RecordingHook("provider2");

        HookedStringProvider provider1 = new HookedStringProvider("provider1", List.of(firstHook), new RuntimeException("boom"));
        HookedStringProvider provider2 = new HookedStringProvider(
                "provider2",
                List.of(secondHook),
                ProviderEvaluation.<String>builder().value("ok").build());

        MultiProvider multiProvider = new MultiProvider(List.of(provider1, provider2), new FirstSuccessfulStrategy());

        OpenFeatureAPI api = new TestOpenFeatureAPI();
        api.shutdown();
        try {
            api.setProviderAndWait("multiProviderHooks", multiProvider);
            Client client = api.getClient("multiProviderHooks");

            var evaluation = client.getStringDetails(
                    "flag",
                    "default",
                    new ImmutableContext(),
                    FlagEvaluationOptions.builder().hookHints(Map.of("hintKey", "hintValue")).build());

            assertEquals("ok", evaluation.getValue());

            assertEquals("hintValue", firstHook.lastHints.get("hintKey"));
            assertEquals("hintValue", secondHook.lastHints.get("hintKey"));
            assertEquals("multiProviderHooks", firstHook.lastClientDomain);
            assertEquals("multiProviderHooks", secondHook.lastClientDomain);

            assertNotNull(firstHook.lastFinallyDetails);
            assertEquals(ErrorCode.GENERAL, firstHook.lastFinallyDetails.getErrorCode());
            assertEquals(Reason.ERROR.toString(), firstHook.lastFinallyDetails.getReason());
            assertEquals("default", firstHook.lastFinallyDetails.getValue());
            assertEquals("boom", firstHook.lastFinallyDetails.getErrorMessage());
        } finally {
            api.shutdown();
        }
    }

    static class RecordingHook implements Hook<String> {
        private final String providerName;
        private final AtomicInteger beforeCount = new AtomicInteger();
        private final AtomicInteger afterCount = new AtomicInteger();
        private final AtomicInteger errorCount = new AtomicInteger();
        private final AtomicInteger finallyCount = new AtomicInteger();
        private Map<String, Object> lastHints = Map.of();
        private String lastClientDomain;
        private FlagEvaluationDetails<String> lastFinallyDetails;

        RecordingHook(String providerName) {
            this.providerName = providerName;
        }

        @Override
        public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
            beforeCount.incrementAndGet();
            ctx.getHookData().set("provider", providerName);
            lastHints = hints;
            lastClientDomain = ctx.getClientMetadata().getDomain();
            return Optional.of(new MutableContext()
                    .add("hookOwner", providerName)
                    .add(providerName + "Marker", providerName));
        }

        @Override
        public void after(
                HookContext<String> ctx,
                dev.openfeature.sdk.FlagEvaluationDetails<String> details,
                Map<String, Object> hints) {
            afterCount.incrementAndGet();
            assertEquals(providerName, ctx.getHookData().get("provider"));
            lastHints = hints;
            lastClientDomain = ctx.getClientMetadata().getDomain();
        }

        @Override
        public void error(HookContext<String> ctx, Exception error, Map<String, Object> hints) {
            errorCount.incrementAndGet();
            assertEquals(providerName, ctx.getHookData().get("provider"));
            lastHints = hints;
            lastClientDomain = ctx.getClientMetadata().getDomain();
        }

        @Override
        public void finallyAfter(
                HookContext<String> ctx,
                dev.openfeature.sdk.FlagEvaluationDetails<String> details,
                Map<String, Object> hints) {
            finallyCount.incrementAndGet();
            assertEquals(providerName, ctx.getHookData().get("provider"));
            lastHints = hints;
            lastClientDomain = ctx.getClientMetadata().getDomain();
            lastFinallyDetails = details;
        }
    }

    static class HookedStringProvider extends EventProvider {
        private final String name;
        private final List<Hook<String>> hooks;
        private final ProviderEvaluation<String> evaluation;
        private final RuntimeException evaluationException;
        private EvaluationContext lastEvaluationContext;

        HookedStringProvider(String name, List<Hook<String>> hooks, ProviderEvaluation<String> evaluation) {
            this.name = name;
            this.hooks = hooks;
            this.evaluation = evaluation;
            this.evaluationException = null;
        }

        HookedStringProvider(String name, List<Hook<String>> hooks, RuntimeException evaluationException) {
            this.name = name;
            this.hooks = hooks;
            this.evaluation = null;
            this.evaluationException = evaluationException;
        }

        @Override
        public Metadata getMetadata() {
            return () -> name;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public List<Hook> getProviderHooks() {
            return List.copyOf(hooks);
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Boolean>builder().value(defaultValue).build();
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            lastEvaluationContext = ctx == null ? new MutableContext() : ctx;
            if (evaluationException != null) {
                throw evaluationException;
            }
            return evaluation;
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Integer>builder().value(defaultValue).build();
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Double>builder().value(defaultValue).build();
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Value>builder().value(defaultValue).build();
        }
    }

    static class TestOpenFeatureAPI extends OpenFeatureAPI {
        TestOpenFeatureAPI() {
            super();
        }
    }
}

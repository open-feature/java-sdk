package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.FlagEvaluationDetails;
import dev.openfeature.api.lifecycle.HookContext;
import dev.openfeature.api.types.ClientMetadata;
import dev.openfeature.api.types.Metadata;
import dev.openfeature.api.types.ProviderMetadata;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class TelemetryTest {

    String flagKey = "test-flag";
    String providerName = "test-provider";
    String reason = "static";
    ProviderMetadata providerMetadata = () -> providerName;

    @Test
    void testCreatesEvaluationEventWithMandatoryFields() {

        var hookContext = generateHookContext(
                flagKey, FlagValueType.BOOLEAN, false, EvaluationContext.EMPTY, null, providerMetadata);
        FlagEvaluationDetails<Boolean> evaluation =
                FlagEvaluationDetails.of(flagKey, true, null, Reason.STATIC, null, null, null);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, evaluation);

        assertEquals(Telemetry.FLAG_EVALUATION_EVENT_NAME, event.getName());
        assertEquals(flagKey, event.getAttributes().get(Telemetry.TELEMETRY_KEY));
        assertEquals(providerName, event.getAttributes().get(Telemetry.TELEMETRY_PROVIDER));
        assertEquals(reason.toLowerCase(), event.getAttributes().get(Telemetry.TELEMETRY_REASON));
    }

    @Test
    void testHandlesNullReason() {
        var hookContext = generateHookContext(
                flagKey, FlagValueType.BOOLEAN, false, EvaluationContext.EMPTY, null, providerMetadata);
        FlagEvaluationDetails<Boolean> evaluation =
                FlagEvaluationDetails.of(flagKey, true, null, (String) null, null, null, null);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, evaluation);

        assertEquals(Reason.UNKNOWN.name().toLowerCase(), event.getAttributes().get(Telemetry.TELEMETRY_REASON));
    }

    @Test
    void testSetsVariantAttributeWhenVariantExists() {
        var hookContext = generateHookContext(
                "testFlag", FlagValueType.STRING, "default", EvaluationContext.EMPTY, () -> "", providerMetadata);

        FlagEvaluationDetails<String> providerEvaluation =
                FlagEvaluationDetails.of(null, null, "testVariant", reason, null, null, Metadata.EMPTY);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("testVariant", event.getAttributes().get(Telemetry.TELEMETRY_VARIANT));
    }

    @Test
    void test_sets_value_in_body_when_variant_is_null() {
        var hookContext = generateHookContext(
                "testFlag", FlagValueType.STRING, "default", EvaluationContext.EMPTY, () -> "", providerMetadata);

        FlagEvaluationDetails<String> providerEvaluation =
                FlagEvaluationDetails.of(null, "testValue", null, reason, null, null, Metadata.EMPTY);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("testValue", event.getAttributes().get(Telemetry.TELEMETRY_VALUE));
    }

    @Test
    void testAllFieldsPopulated() {
        var hookContext = generateHookContext(
                "realFlag",
                FlagValueType.STRING,
                "realDefault",
                EvaluationContext.immutableOf("realTargetingKey", Map.of()),
                () -> "",
                () -> "realProviderName");
        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.of(
                null,
                null,
                "realVariant",
                Reason.DEFAULT.name(),
                null,
                null,
                Metadata.immutableBuilder()
                        .add("contextId", "realContextId")
                        .add("flagSetId", "realFlagSetId")
                        .add("version", "realVersion")
                        .build());

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("realFlag", event.getAttributes().get(Telemetry.TELEMETRY_KEY));
        assertEquals("realProviderName", event.getAttributes().get(Telemetry.TELEMETRY_PROVIDER));
        assertEquals("default", event.getAttributes().get(Telemetry.TELEMETRY_REASON));
        assertEquals("realContextId", event.getAttributes().get(Telemetry.TELEMETRY_CONTEXT_ID));
        assertEquals("realFlagSetId", event.getAttributes().get(Telemetry.TELEMETRY_FLAG_SET_ID));
        assertEquals("realVersion", event.getAttributes().get(Telemetry.TELEMETRY_VERSION));
        assertNull(event.getAttributes().get(Telemetry.TELEMETRY_ERROR_CODE));
        assertEquals("realVariant", event.getAttributes().get(Telemetry.TELEMETRY_VARIANT));
    }

    @Test
    void testErrorEvaluation() {
        var hookContext = generateHookContext(
                "realFlag",
                FlagValueType.STRING,
                "realDefault",
                EvaluationContext.immutableOf("realTargetingKey", Map.of()),
                () -> "",
                () -> "realProviderName");

        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.of(
                null,
                null,
                null,
                Reason.ERROR.name(),
                null,
                "realErrorMessage",
                Metadata.immutableBuilder()
                        .add("contextId", "realContextId")
                        .add("flagSetId", "realFlagSetId")
                        .add("version", "realVersion")
                        .build());

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("realFlag", event.getAttributes().get(Telemetry.TELEMETRY_KEY));
        assertEquals("realProviderName", event.getAttributes().get(Telemetry.TELEMETRY_PROVIDER));
        assertEquals("error", event.getAttributes().get(Telemetry.TELEMETRY_REASON));
        assertEquals("realContextId", event.getAttributes().get(Telemetry.TELEMETRY_CONTEXT_ID));
        assertEquals("realFlagSetId", event.getAttributes().get(Telemetry.TELEMETRY_FLAG_SET_ID));
        assertEquals("realVersion", event.getAttributes().get(Telemetry.TELEMETRY_VERSION));
        assertEquals(ErrorCode.GENERAL, event.getAttributes().get(Telemetry.TELEMETRY_ERROR_CODE));
        assertEquals("realErrorMessage", event.getAttributes().get(Telemetry.TELEMETRY_ERROR_MSG));
        assertNull(event.getAttributes().get(Telemetry.TELEMETRY_VARIANT));
    }

    @Test
    void testErrorCodeEvaluation() {
        var hookContext = generateHookContext(
                "realFlag",
                FlagValueType.STRING,
                "realDefault",
                EvaluationContext.immutableOf("realTargetingKey", Map.of()),
                () -> "",
                () -> "realProviderName");

        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.of(
                null,
                null,
                null,
                Reason.ERROR.name(),
                ErrorCode.INVALID_CONTEXT,
                "realErrorMessage",
                Metadata.immutableBuilder()
                        .add("contextId", "realContextId")
                        .add("flagSetId", "realFlagSetId")
                        .add("version", "realVersion")
                        .build());

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("realFlag", event.getAttributes().get(Telemetry.TELEMETRY_KEY));
        assertEquals("realProviderName", event.getAttributes().get(Telemetry.TELEMETRY_PROVIDER));
        assertEquals("error", event.getAttributes().get(Telemetry.TELEMETRY_REASON));
        assertEquals("realContextId", event.getAttributes().get(Telemetry.TELEMETRY_CONTEXT_ID));
        assertEquals("realFlagSetId", event.getAttributes().get(Telemetry.TELEMETRY_FLAG_SET_ID));
        assertEquals("realVersion", event.getAttributes().get(Telemetry.TELEMETRY_VERSION));
        assertEquals(ErrorCode.INVALID_CONTEXT, event.getAttributes().get(Telemetry.TELEMETRY_ERROR_CODE));
        assertEquals("realErrorMessage", event.getAttributes().get(Telemetry.TELEMETRY_ERROR_MSG));
        assertNull(event.getAttributes().get(Telemetry.TELEMETRY_VARIANT));
    }

    private <T> HookContext<T> generateHookContext(
            final String flagKey,
            final FlagValueType type,
            final T defaultValue,
            final EvaluationContext ctx,
            final ClientMetadata clientMetadata,
            final ProviderMetadata providerMeta) {
        return new HookContext<T>() {

            @Override
            public String getFlagKey() {
                return flagKey;
            }

            @Override
            public FlagValueType getType() {
                return type;
            }

            @Override
            public T getDefaultValue() {
                return defaultValue;
            }

            @Override
            public EvaluationContext getCtx() {
                return ctx;
            }

            @Override
            public ClientMetadata getClientMetadata() {
                return clientMetadata;
            }

            @Override
            public ProviderMetadata getProviderMetadata() {
                return providerMeta;
            }
        };
    }
}

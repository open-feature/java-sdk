package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class TelemetryTest {

    String flagKey = "test-flag";
    String providerName = "test-provider";
    String reason = "static";
    ProviderMetadata providerMetadata = () -> providerName;

    @Test
    void testCreatesEvaluationEventWithMandatoryFields() {
        HookContext<Boolean> hookContext = HookContext.<Boolean>builder()
                .flagKey(flagKey)
                .providerMetadata(providerMetadata)
                .type(FlagValueType.BOOLEAN)
                .defaultValue(false)
                .ctx(new ImmutableContext())
                .build();

        FlagEvaluationDetails<Boolean> evaluation =
                new DefaultFlagEvaluationDetails<>(flagKey, true, null, reason, null, null, null);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, evaluation);

        assertEquals(Telemetry.FLAG_EVALUATION_EVENT_NAME, event.getName());
        assertEquals(flagKey, event.getAttributes().get(Telemetry.TELEMETRY_KEY));
        assertEquals(providerName, event.getAttributes().get(Telemetry.TELEMETRY_PROVIDER));
        assertEquals(reason.toLowerCase(), event.getAttributes().get(Telemetry.TELEMETRY_REASON));
    }

    @Test
    void testHandlesNullReason() {
        HookContext<Boolean> hookContext = HookContext.<Boolean>builder()
                .flagKey(flagKey)
                .providerMetadata(providerMetadata)
                .type(FlagValueType.BOOLEAN)
                .defaultValue(false)
                .ctx(new ImmutableContext())
                .build();

        FlagEvaluationDetails<Boolean> evaluation =
                new DefaultFlagEvaluationDetails<>(flagKey, true, null, null, null, null, null);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, evaluation);

        assertEquals(Reason.UNKNOWN.name().toLowerCase(), event.getAttributes().get(Telemetry.TELEMETRY_REASON));
    }

    @Test
    void testSetsVariantAttributeWhenVariantExists() {
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("testFlag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(EvaluationContext.EMPTY)
                .clientMetadata(() -> "")
                .providerMetadata(providerMetadata)
                .build();

        FlagEvaluationDetails<String> providerEvaluation =
                new DefaultFlagEvaluationDetails<>(null, null, "testVariant", reason, null, null, Metadata.EMPTY);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("testVariant", event.getAttributes().get(Telemetry.TELEMETRY_VARIANT));
    }

    @Test
    void test_sets_value_in_body_when_variant_is_null() {
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("testFlag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(EvaluationContext.EMPTY)
                .clientMetadata(() -> "")
                .providerMetadata(providerMetadata)
                .build();

        FlagEvaluationDetails<String> providerEvaluation =
                new DefaultFlagEvaluationDetails<>(null, "testValue", null, reason, null, null, Metadata.EMPTY);

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("testValue", event.getAttributes().get(Telemetry.TELEMETRY_VALUE));
    }

    @Test
    void testAllFieldsPopulated() {
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("realFlag")
                .type(FlagValueType.STRING)
                .defaultValue("realDefault")
                .ctx(new ImmutableContext("realTargetingKey", Map.of()))
                .clientMetadata(() -> "")
                .providerMetadata(() -> "realProviderName")
                .build();

        FlagEvaluationDetails<String> providerEvaluation = new DefaultFlagEvaluationDetails<>(
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
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("realFlag")
                .type(FlagValueType.STRING)
                .defaultValue("realDefault")
                .ctx(new ImmutableContext("realTargetingKey", Map.of()))
                .clientMetadata(() -> "")
                .providerMetadata(() -> "realProviderName")
                .build();

        FlagEvaluationDetails<String> providerEvaluation = new DefaultFlagEvaluationDetails<>(
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
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("realFlag")
                .type(FlagValueType.STRING)
                .defaultValue("realDefault")
                .ctx(new ImmutableContext("realTargetingKey", Map.of()))
                .clientMetadata(() -> "")
                .providerMetadata(() -> "realProviderName")
                .build();

        FlagEvaluationDetails<String> providerEvaluation = new DefaultFlagEvaluationDetails<>(
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
}

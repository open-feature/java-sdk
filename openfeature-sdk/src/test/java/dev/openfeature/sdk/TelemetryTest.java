package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.openfeature.api.ClientMetadata;
import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.EvaluationEvent;
import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.HookContext;
import dev.openfeature.api.ImmutableContext;
import dev.openfeature.api.ImmutableMetadata;
import dev.openfeature.api.Metadata;
import dev.openfeature.api.Reason;
import dev.openfeature.api.Telemetry;
import org.junit.jupiter.api.Test;

public class TelemetryTest {

    @Test
    void testCreatesEvaluationEventWithMandatoryFields() {
        // Arrange
        String flagKey = "test-flag";
        String providerName = "test-provider";
        String reason = "static";

        Metadata providerMetadata = mock(Metadata.class);
        when(providerMetadata.getName()).thenReturn(providerName);

        HookContext<Boolean> hookContext = HookContext.<Boolean>builder()
                .flagKey(flagKey)
                .providerMetadata(providerMetadata)
                .type(FlagValueType.BOOLEAN)
                .defaultValue(false)
                .ctx(new ImmutableContext())
                .build();

        FlagEvaluationDetails<Boolean> evaluation = FlagEvaluationDetails.<Boolean>builder()
                .reason(reason)
                .value(true)
                .build();

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, evaluation);

        assertEquals(Telemetry.FLAG_EVALUATION_EVENT_NAME, event.getName());
        assertEquals(flagKey, event.getAttributes().get(Telemetry.TELEMETRY_KEY));
        assertEquals(providerName, event.getAttributes().get(Telemetry.TELEMETRY_PROVIDER));
        assertEquals(reason.toLowerCase(), event.getAttributes().get(Telemetry.TELEMETRY_REASON));
    }

    @Test
    void testHandlesNullReason() {
        // Arrange
        String flagKey = "test-flag";
        String providerName = "test-provider";

        Metadata providerMetadata = mock(Metadata.class);
        when(providerMetadata.getName()).thenReturn(providerName);

        HookContext<Boolean> hookContext = HookContext.<Boolean>builder()
                .flagKey(flagKey)
                .providerMetadata(providerMetadata)
                .type(FlagValueType.BOOLEAN)
                .defaultValue(false)
                .ctx(new ImmutableContext())
                .build();

        FlagEvaluationDetails<Boolean> evaluation = FlagEvaluationDetails.<Boolean>builder()
                .reason(null)
                .value(true)
                .build();

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, evaluation);

        assertEquals(Reason.UNKNOWN.name().toLowerCase(), event.getAttributes().get(Telemetry.TELEMETRY_REASON));
    }

    @Test
    void testSetsVariantAttributeWhenVariantExists() {
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("testFlag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(mock(EvaluationContext.class))
                .clientMetadata(mock(ClientMetadata.class))
                .providerMetadata(mock(Metadata.class))
                .build();

        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.<String>builder()
                .variant("testVariant")
                .flagMetadata(ImmutableMetadata.builder().build())
                .build();

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("testVariant", event.getAttributes().get(Telemetry.TELEMETRY_VARIANT));
    }

    @Test
    void test_sets_value_in_body_when_variant_is_null() {
        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("testFlag")
                .type(FlagValueType.STRING)
                .defaultValue("default")
                .ctx(mock(EvaluationContext.class))
                .clientMetadata(mock(ClientMetadata.class))
                .providerMetadata(mock(Metadata.class))
                .build();

        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.<String>builder()
                .value("testValue")
                .flagMetadata(ImmutableMetadata.builder().build())
                .build();

        EvaluationEvent event = Telemetry.createEvaluationEvent(hookContext, providerEvaluation);

        assertEquals("testValue", event.getAttributes().get(Telemetry.TELEMETRY_VALUE));
    }

    @Test
    void testAllFieldsPopulated() {
        EvaluationContext evaluationContext = mock(EvaluationContext.class);
        when(evaluationContext.getTargetingKey()).thenReturn("realTargetingKey");

        Metadata providerMetadata = mock(Metadata.class);
        when(providerMetadata.getName()).thenReturn("realProviderName");

        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("realFlag")
                .type(FlagValueType.STRING)
                .defaultValue("realDefault")
                .ctx(evaluationContext)
                .clientMetadata(mock(ClientMetadata.class))
                .providerMetadata(providerMetadata)
                .build();

        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.<String>builder()
                .flagMetadata(ImmutableMetadata.builder()
                        .addString("contextId", "realContextId")
                        .addString("flagSetId", "realFlagSetId")
                        .addString("version", "realVersion")
                        .build())
                .reason(Reason.DEFAULT.name())
                .variant("realVariant")
                .build();

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
        EvaluationContext evaluationContext = mock(EvaluationContext.class);
        when(evaluationContext.getTargetingKey()).thenReturn("realTargetingKey");

        Metadata providerMetadata = mock(Metadata.class);
        when(providerMetadata.getName()).thenReturn("realProviderName");

        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("realFlag")
                .type(FlagValueType.STRING)
                .defaultValue("realDefault")
                .ctx(evaluationContext)
                .clientMetadata(mock(ClientMetadata.class))
                .providerMetadata(providerMetadata)
                .build();

        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.<String>builder()
                .flagMetadata(ImmutableMetadata.builder()
                        .addString("contextId", "realContextId")
                        .addString("flagSetId", "realFlagSetId")
                        .addString("version", "realVersion")
                        .build())
                .reason(Reason.ERROR.name())
                .errorMessage("realErrorMessage")
                .build();

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
        EvaluationContext evaluationContext = mock(EvaluationContext.class);
        when(evaluationContext.getTargetingKey()).thenReturn("realTargetingKey");

        Metadata providerMetadata = mock(Metadata.class);
        when(providerMetadata.getName()).thenReturn("realProviderName");

        HookContext<String> hookContext = HookContext.<String>builder()
                .flagKey("realFlag")
                .type(FlagValueType.STRING)
                .defaultValue("realDefault")
                .ctx(evaluationContext)
                .clientMetadata(mock(ClientMetadata.class))
                .providerMetadata(providerMetadata)
                .build();

        FlagEvaluationDetails<String> providerEvaluation = FlagEvaluationDetails.<String>builder()
                .flagMetadata(ImmutableMetadata.builder()
                        .addString("contextId", "realContextId")
                        .addString("flagSetId", "realFlagSetId")
                        .addString("version", "realVersion")
                        .build())
                .reason(Reason.ERROR.name())
                .errorMessage("realErrorMessage")
                .errorCode(ErrorCode.INVALID_CONTEXT)
                .build();

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

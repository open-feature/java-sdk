package dev.openfeature.sdk;

/**
 * The Telemetry class provides constants and methods for creating OpenTelemetry compliant
 * evaluation events.
 */
public class Telemetry {

    private Telemetry() {}

    /*
    The OpenTelemetry compliant event attributes for flag evaluation.
    Specification: https://opentelemetry.io/docs/specs/semconv/feature-flags/feature-flags-logs/
     */
    public static final String TELEMETRY_KEY = "feature_flag.key";
    public static final String TELEMETRY_ERROR_CODE = "error.type";
    public static final String TELEMETRY_VARIANT = "feature_flag.result.variant";
    public static final String TELEMETRY_VALUE = "feature_flag.result.value";
    public static final String TELEMETRY_CONTEXT_ID = "feature_flag.context.id";
    public static final String TELEMETRY_ERROR_MSG = "feature_flag.evaluation.error.message";
    public static final String TELEMETRY_REASON = "feature_flag.result.reason";
    public static final String TELEMETRY_PROVIDER = "feature_flag.provider.name";
    public static final String TELEMETRY_FLAG_SET_ID = "feature_flag.set.id";
    public static final String TELEMETRY_VERSION = "feature_flag.version";

    // Well-known flag metadata attributes for telemetry events.
    // Specification: https://openfeature.dev/specification/appendix-d#flag-metadata
    public static final String TELEMETRY_FLAG_META_CONTEXT_ID = "contextId";
    public static final String TELEMETRY_FLAG_META_FLAG_SET_ID = "flagSetId";
    public static final String TELEMETRY_FLAG_META_VERSION = "version";

    public static final String FLAG_EVALUATION_EVENT_NAME = "feature_flag.evaluation";

    /**
     * Creates an EvaluationEvent using the provided HookContext and ProviderEvaluation.
     *
     * @param hookContext the context containing flag evaluation details
     * @param evaluationDetails the evaluation result from the provider
     *
     * @return an EvaluationEvent populated with telemetry data
     */
    public static EvaluationEvent createEvaluationEvent(
            HookContext<?> hookContext, FlagEvaluationDetails<?> evaluationDetails) {
        EvaluationEvent.EvaluationEventBuilder evaluationEventBuilder = EvaluationEvent.builder()
                .name(FLAG_EVALUATION_EVENT_NAME)
                .attribute(TELEMETRY_KEY, hookContext.getFlagKey())
                .attribute(TELEMETRY_PROVIDER, hookContext.getProviderMetadata().getName());

        if (evaluationDetails.getReason() != null) {
            evaluationEventBuilder.attribute(
                    TELEMETRY_REASON, evaluationDetails.getReason().toLowerCase());
        } else {
            evaluationEventBuilder.attribute(
                    TELEMETRY_REASON, Reason.UNKNOWN.name().toLowerCase());
        }

        if (evaluationDetails.getVariant() != null) {
            evaluationEventBuilder.attribute(TELEMETRY_VARIANT, evaluationDetails.getVariant());
        } else {
            evaluationEventBuilder.attribute(TELEMETRY_VALUE, evaluationDetails.getValue());
        }

        String contextId = evaluationDetails.getFlagMetadata().getString(TELEMETRY_FLAG_META_CONTEXT_ID);
        if (contextId != null) {
            evaluationEventBuilder.attribute(TELEMETRY_CONTEXT_ID, contextId);
        } else {
            evaluationEventBuilder.attribute(
                    TELEMETRY_CONTEXT_ID, hookContext.getCtx().getTargetingKey());
        }

        String setID = evaluationDetails.getFlagMetadata().getString(TELEMETRY_FLAG_META_FLAG_SET_ID);
        if (setID != null) {
            evaluationEventBuilder.attribute(TELEMETRY_FLAG_SET_ID, setID);
        }

        String version = evaluationDetails.getFlagMetadata().getString(TELEMETRY_FLAG_META_VERSION);
        if (version != null) {
            evaluationEventBuilder.attribute(TELEMETRY_VERSION, version);
        }

        if (Reason.ERROR.name().equals(evaluationDetails.getReason())) {
            if (evaluationDetails.getErrorCode() != null) {
                evaluationEventBuilder.attribute(TELEMETRY_ERROR_CODE, evaluationDetails.getErrorCode());
            } else {
                evaluationEventBuilder.attribute(TELEMETRY_ERROR_CODE, ErrorCode.GENERAL);
            }

            if (evaluationDetails.getErrorMessage() != null) {
                evaluationEventBuilder.attribute(TELEMETRY_ERROR_MSG, evaluationDetails.getErrorMessage());
            }
        }

        return evaluationEventBuilder.build();
    }
}

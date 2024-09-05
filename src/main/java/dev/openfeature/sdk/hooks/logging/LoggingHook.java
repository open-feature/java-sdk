package dev.openfeature.sdk.hooks.logging;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * A hook for logging flag evaluations.
 * Useful for debugging.
 * Flag evaluation data is logged at debug and error in before/after stages and error stages, respectively.
 */
@Slf4j
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED",
    justification = "we can ignore return values of chainables (builders) here")
public class LoggingHook implements Hook<Object> {

    static final String DOMAIN_KEY = "domain";
    static final String PROVIDER_NAME_KEY = "provider_name";
    static final String FLAG_KEY_KEY = "flag_key";
    static final String DEFAULT_VALUE_KEY = "default_value";
    static final String EVALUATION_CONTEXT_KEY = "evaluation_context";
    static final String ERROR_CODE_KEY = "error_code";
    static final String ERROR_MESSAGE_KEY = "error_message";
    static final String REASON_KEY = "reason";
    static final String VARIANT_KEY = "variant";
    static final String VALUE_KEY = "value";

    private boolean includeEvaluationContext;

    /**
     * Construct a new LoggingHook.
     */
    public LoggingHook() {
        this(false);
    }

    /**
     * Construct a new LoggingHook.
     * @param includeEvaluationContext include a serialized evaluation context in the log message (defaults to false)
     */
    public LoggingHook(boolean includeEvaluationContext) {
        this.includeEvaluationContext = includeEvaluationContext;
    }

    @Override
    public Optional<EvaluationContext> before(HookContext<Object> hookContext, Map<String, Object> hints) {
        LoggingEventBuilder builder = log.atDebug();
        addCommonProps(builder, hookContext);
        builder.log("Before stage");

        return Optional.empty();
    }

    @Override
    public void after(HookContext<Object> hookContext, FlagEvaluationDetails<Object> details,
            Map<String, Object> hints) {
        LoggingEventBuilder builder = log.atDebug()
                .addKeyValue(REASON_KEY, details.getReason())
                .addKeyValue(VARIANT_KEY, details.getVariant())
                .addKeyValue(VALUE_KEY, details.getValue());
        addCommonProps(builder, hookContext);
        builder.log("After stage");
    }

    @Override
    public void error(HookContext<Object> hookContext, Exception error, Map<String, Object> hints) {
        LoggingEventBuilder builder = log.atError()
                .addKeyValue(ERROR_MESSAGE_KEY, error.getMessage());
        addCommonProps(builder, hookContext);
        ErrorCode errorCode = error instanceof OpenFeatureError ? ((OpenFeatureError) error).getErrorCode() : null;
        builder.addKeyValue(ERROR_CODE_KEY, errorCode);
        builder.log("Error stage", error);
    }

    private void addCommonProps(LoggingEventBuilder builder, HookContext<Object> hookContext) {
        builder.addKeyValue(DOMAIN_KEY, hookContext.getClientMetadata().getDomain())
                .addKeyValue(PROVIDER_NAME_KEY, hookContext.getProviderMetadata().getName())
                .addKeyValue(FLAG_KEY_KEY, hookContext.getFlagKey())
                .addKeyValue(DEFAULT_VALUE_KEY, hookContext.getDefaultValue());

        if (includeEvaluationContext) {
            builder.addKeyValue(EVALUATION_CONTEXT_KEY, hookContext.getCtx());
        }
    }
}

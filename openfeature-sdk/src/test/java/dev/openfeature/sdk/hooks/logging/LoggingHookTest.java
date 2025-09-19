package dev.openfeature.sdk.hooks.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.FlagValueType;
import dev.openfeature.api.Reason;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.FlagEvaluationDetails;
import dev.openfeature.api.exceptions.GeneralError;
import dev.openfeature.api.lifecycle.HookContext;
import dev.openfeature.api.types.ClientMetadata;
import dev.openfeature.api.types.ProviderMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;

class LoggingHookTest {

    private static final String FLAG_KEY = "some-key";
    private static final String DEFAULT_VALUE = "default";
    private static final String DOMAIN = "some-domain";
    private static final String PROVIDER_NAME = "some-provider";
    private static final Reason REASON = Reason.DEFAULT;
    private static final String VALUE = "some-value";
    private static final String VARIANT = "some-variant";
    private static final String ERROR_MESSAGE = "some fake error!";
    private static final ErrorCode ERROR_CODE = ErrorCode.GENERAL;

    private HookContext<Object> hookContext;
    private LoggingEventBuilder mockBuilder;
    private Logger logger;

    @BeforeEach
    void each() {

        // create a fake hook context
        hookContext = new HookContext<>() {
            @Override
            public String getFlagKey() {
                return FLAG_KEY;
            }

            @Override
            public FlagValueType getType() {
                return FlagValueType.BOOLEAN;
            }

            @Override
            public Object getDefaultValue() {
                return DEFAULT_VALUE;
            }

            @Override
            public EvaluationContext getCtx() {
                return EvaluationContext.EMPTY;
            }

            @Override
            public ClientMetadata getClientMetadata() {
                return () -> DOMAIN;
            }

            @Override
            public ProviderMetadata getProviderMetadata() {
                return () -> PROVIDER_NAME;
            }
        };

        // mock logging
        logger = mock(Logger.class);
        mockBuilder = mock(LoggingEventBuilder.class);
        when(mockBuilder.addKeyValue(anyString(), anyString())).thenReturn(mockBuilder);
        when(logger.atDebug()).thenReturn(mockBuilder);
        when(logger.atError()).thenReturn(mockBuilder);
        LoggerMock.setMock(LoggingHook.class, logger);
    }

    @Test
    void beforeLogsAllPropsExceptEvaluationContext() throws Exception {
        LoggingHook hook = new LoggingHook();
        hook.before(hookContext, null);

        verify(logger).atDebug();
        verifyCommonProps(mockBuilder);
        verify(mockBuilder, never()).addKeyValue(anyString(), any(EvaluationContext.class));
        verify(mockBuilder).log(argThat((String s) -> s.contains("Before")));
    }

    @Test
    void beforeLogsAllPropsAndEvaluationContext() throws Exception {
        LoggingHook hook = new LoggingHook(true);
        hook.before(hookContext, null);

        verify(logger).atDebug();
        verifyCommonProps(mockBuilder);
        verify(mockBuilder).addKeyValue(contains(LoggingHook.EVALUATION_CONTEXT_KEY), any(EvaluationContext.class));
        verify(mockBuilder).log(argThat((String s) -> s.contains("Before")));
    }

    @Test
    void afterLogsAllPropsExceptEvaluationContext() throws Exception {
        LoggingHook hook = new LoggingHook();
        FlagEvaluationDetails<Object> details = FlagEvaluationDetails.of("", VALUE, VARIANT, REASON);
        hook.after(hookContext, details, null);

        verify(logger).atDebug();
        verifyAfterProps(mockBuilder);
        verifyCommonProps(mockBuilder);
        verify(mockBuilder, never()).addKeyValue(anyString(), any(EvaluationContext.class));
        verify(mockBuilder).log(argThat((String s) -> s.contains("After")));
    }

    @Test
    void afterLogsAllPropsAndEvaluationContext() throws Exception {
        LoggingHook hook = new LoggingHook(true);
        FlagEvaluationDetails<Object> details = FlagEvaluationDetails.of("", VALUE, VARIANT, REASON);
        hook.after(hookContext, details, null);

        verify(logger).atDebug();
        verifyAfterProps(mockBuilder);
        verifyCommonProps(mockBuilder);
        verify(mockBuilder).addKeyValue(contains(LoggingHook.EVALUATION_CONTEXT_KEY), any(EvaluationContext.class));
        verify(mockBuilder).log(argThat((String s) -> s.contains("After")));
    }

    @Test
    void errorLogsAllPropsExceptEvaluationContext() throws Exception {
        LoggingHook hook = new LoggingHook();
        GeneralError error = new GeneralError(ERROR_MESSAGE);
        hook.error(hookContext, error, null);

        verify(logger).atError();
        verifyCommonProps(mockBuilder);
        verifyErrorProps(mockBuilder);
        verify(mockBuilder, never()).addKeyValue(anyString(), any(EvaluationContext.class));
        verify(mockBuilder).log(argThat((String s) -> s.contains("Error")), any(Exception.class));
    }

    @Test
    void errorLogsAllPropsAndEvaluationContext() throws Exception {
        LoggingHook hook = new LoggingHook(true);
        GeneralError error = new GeneralError(ERROR_MESSAGE);
        hook.error(hookContext, error, null);

        verify(logger).atError();
        verifyCommonProps(mockBuilder);
        verifyErrorProps(mockBuilder);
        verify(mockBuilder).addKeyValue(contains(LoggingHook.EVALUATION_CONTEXT_KEY), any(EvaluationContext.class));
        verify(mockBuilder).log(argThat((String s) -> s.contains("Error")), any(Exception.class));
    }

    private void verifyCommonProps(LoggingEventBuilder mockBuilder) {
        verify(mockBuilder).addKeyValue(LoggingHook.DOMAIN_KEY, DOMAIN);
        verify(mockBuilder).addKeyValue(LoggingHook.FLAG_KEY_KEY, FLAG_KEY);
        verify(mockBuilder).addKeyValue(LoggingHook.PROVIDER_NAME_KEY, PROVIDER_NAME);
        verify(mockBuilder).addKeyValue(LoggingHook.DEFAULT_VALUE_KEY, DEFAULT_VALUE);
    }

    private void verifyAfterProps(LoggingEventBuilder mockBuilder) {
        verify(mockBuilder).addKeyValue(LoggingHook.REASON_KEY, REASON.toString());
        verify(mockBuilder).addKeyValue(LoggingHook.VARIANT_KEY, VARIANT);
        verify(mockBuilder).addKeyValue(LoggingHook.VALUE_KEY, VALUE);
    }

    private void verifyErrorProps(LoggingEventBuilder mockBuilder) {
        verify(mockBuilder).addKeyValue(LoggingHook.ERROR_CODE_KEY, ERROR_CODE);
        verify(mockBuilder).addKeyValue(LoggingHook.ERROR_MESSAGE_KEY, ERROR_MESSAGE);
    }
}

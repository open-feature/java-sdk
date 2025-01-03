package dev.openfeature.sdk.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.openfeature.sdk.ErrorCode;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class ExceptionUtilsTest {

    @ParameterizedTest
    @DisplayName("should produce correct exception for a provided ErrorCode")
    @ArgumentsSource(ErrorCodeTestParameters.class)
    void shouldProduceCorrectExceptionForErrorCode(ErrorCode errorCode, Class<? extends OpenFeatureError> exception) {

        String errorMessage = "error message";
        OpenFeatureError openFeatureError = ExceptionUtils.instantiateErrorByErrorCode(errorCode, errorMessage);
        assertInstanceOf(exception, openFeatureError);
        assertThat(openFeatureError.getMessage()).isEqualTo(errorMessage);
        assertThat(openFeatureError.getErrorCode()).isEqualByComparingTo(errorCode);
    }

    static class ErrorCodeTestParameters implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(ErrorCode.GENERAL, GeneralError.class),
                    Arguments.of(ErrorCode.FLAG_NOT_FOUND, FlagNotFoundError.class),
                    Arguments.of(ErrorCode.PROVIDER_NOT_READY, ProviderNotReadyError.class),
                    Arguments.of(ErrorCode.INVALID_CONTEXT, InvalidContextError.class),
                    Arguments.of(ErrorCode.PARSE_ERROR, ParseError.class),
                    Arguments.of(ErrorCode.TARGETING_KEY_MISSING, TargetingKeyMissingError.class),
                    Arguments.of(ErrorCode.TYPE_MISMATCH, TypeMismatchError.class));
        }
    }
}

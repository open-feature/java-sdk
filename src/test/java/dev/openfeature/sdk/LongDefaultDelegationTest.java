package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the default {@link FeatureProvider#getLongEvaluation(String, Long, EvaluationContext)}
 * delegation behavior.
 */
class LongDefaultDelegationTest {

    /**
     * A FeatureProvider that records calls to getDoubleEvaluation and returns a configurable
     * Double, exercising only the default getLongEvaluation impl.
     */
    private static final class StubDoubleProvider implements FeatureProvider {
        private final Double valueToReturn;
        final List<Double> capturedDefaults = new ArrayList<>();

        StubDoubleProvider(Double valueToReturn) {
            this.valueToReturn = valueToReturn;
        }

        @Override
        public Metadata getMetadata() {
            return () -> "stub";
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext c) {
            capturedDefaults.add(defaultValue);
            return ProviderEvaluation.<Double>builder()
                    .value(valueToReturn)
                    .reason(Reason.STATIC.name())
                    .variant("v")
                    .build();
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext c) {
            throw new UnsupportedOperationException();
        }
    }

    @Nested
    @DisplayName("Successful conversions")
    class Successful {

        @Test
        void convertsIntegerValuedDoubleToLong() {
            var provider = new StubDoubleProvider(42.0);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getValue()).isEqualTo(42L);
            assertThat(result.getReason()).isEqualTo(Reason.STATIC.name());
            assertThat(result.getErrorCode()).isNull();
        }

        @Test
        void convertsZero() {
            var provider = new StubDoubleProvider(0.0);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getValue()).isEqualTo(0L);
            assertThat(result.getErrorCode()).isNull();
        }

        @Test
        void convertsNegativeZeroToZeroLong() {
            var provider = new StubDoubleProvider(-0.0);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getValue()).isEqualTo(0L);
            assertThat(result.getErrorCode()).isNull();
        }

        @Test
        void convertsAtMaxSafeInteger() {
            // 2^53 - 1
            long maxSafe = 9_007_199_254_740_991L;
            var provider = new StubDoubleProvider((double) maxSafe);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getValue()).isEqualTo(maxSafe);
            assertThat(result.getErrorCode()).isNull();
        }

        @Test
        void convertsAtNegativeMaxSafeInteger() {
            long minSafe = -9_007_199_254_740_991L;
            var provider = new StubDoubleProvider((double) minSafe);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getValue()).isEqualTo(minSafe);
            assertThat(result.getErrorCode()).isNull();
        }

        @Test
        void passesThroughErrorMetadataFromProvider() {
            var provider = new FeatureProvider() {
                @Override
                public Metadata getMetadata() {
                    return () -> "stub";
                }

                @Override
                public ProviderEvaluation<Boolean> getBooleanEvaluation(String k, Boolean d, EvaluationContext c) {
                    return null;
                }

                @Override
                public ProviderEvaluation<String> getStringEvaluation(String k, String d, EvaluationContext c) {
                    return null;
                }

                @Override
                public ProviderEvaluation<Integer> getIntegerEvaluation(String k, Integer d, EvaluationContext c) {
                    return null;
                }

                @Override
                public ProviderEvaluation<Double> getDoubleEvaluation(String k, Double d, EvaluationContext c) {
                    return ProviderEvaluation.<Double>builder()
                            .errorCode(ErrorCode.FLAG_NOT_FOUND)
                            .errorMessage("nope")
                            .build();
                }

                @Override
                public ProviderEvaluation<Value> getObjectEvaluation(String k, Value d, EvaluationContext c) {
                    return null;
                }
            };
            var result = provider.getLongEvaluation("k", 99L, new ImmutableContext());
            // null double should fall back to the user's Long default
            assertThat(result.getValue()).isEqualTo(99L);
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.FLAG_NOT_FOUND);
            assertThat(result.getErrorMessage()).isEqualTo("nope");
        }

        @Test
        void passesLongDefaultAsDoubleToProvider() {
            var provider = new StubDoubleProvider(0.0);
            provider.getLongEvaluation("k", 12345L, new ImmutableContext());
            assertThat(provider.capturedDefaults).containsExactly(12345.0);
        }

        @Test
        void passesNullDefaultAsNullToProvider() {
            var provider = new StubDoubleProvider(0.0);
            provider.getLongEvaluation("k", null, new ImmutableContext());
            assertThat(provider.capturedDefaults).containsExactly((Double) null);
        }
    }

    @Nested
    @DisplayName("Bound violations return TYPE_MISMATCH")
    class Bounds {

        @Test
        void returnsTypeMismatchAtTwoToTheFiftyThree() {
            // 2^53 ; representable as double, but outside the JS-safe-integer range
            var provider = new StubDoubleProvider(9_007_199_254_740_992.0);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
            assertThat(result.getReason()).isEqualTo(Reason.ERROR.toString());
            assertThat(result.getValue()).isEqualTo(0L);
        }

        @Test
        void returnsTypeMismatchAboveTwoToTheFiftyThree() {
            var provider = new StubDoubleProvider(1e16);
            var result = provider.getLongEvaluation("k", 7L, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
            assertThat(result.getValue()).isEqualTo(7L);
        }

        @Test
        void returnsTypeMismatchBelowNegativeTwoToTheFiftyThree() {
            var provider = new StubDoubleProvider(-9_007_199_254_740_992.0);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
        }

        @Test
        void returnsTypeMismatchWhenLongDefaultExceedsSafeRange() {
            var provider = new StubDoubleProvider(0.0);
            var result = provider.getLongEvaluation("k", Long.MAX_VALUE, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
            assertThat(result.getValue()).isEqualTo(Long.MAX_VALUE);
            // provider was never called, so no default captured
            assertThat(provider.capturedDefaults).isEmpty();
        }

        @Test
        void returnsTypeMismatchWhenNegativeLongDefaultExceedsSafeRange() {
            var provider = new StubDoubleProvider(0.0);
            var result = provider.getLongEvaluation("k", Long.MIN_VALUE, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
        }
    }

    @Nested
    @DisplayName("Non-integer doubles return TYPE_MISMATCH")
    class NonInteger {

        @Test
        void returnsTypeMismatchOnFractional() {
            var provider = new StubDoubleProvider(1.5);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
            assertThat(result.getValue()).isEqualTo(0L);
        }

        @Test
        void returnsTypeMismatchOnNaN() {
            var provider = new StubDoubleProvider(Double.NaN);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
        }

        @Test
        void returnsTypeMismatchOnPositiveInfinity() {
            var provider = new StubDoubleProvider(Double.POSITIVE_INFINITY);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
        }

        @Test
        void returnsTypeMismatchOnNegativeInfinity() {
            var provider = new StubDoubleProvider(Double.NEGATIVE_INFINITY);
            var result = provider.getLongEvaluation("k", 0L, new ImmutableContext());
            assertThat(result.getErrorCode()).isEqualTo(ErrorCode.TYPE_MISMATCH);
        }
    }
}

package dev.openfeature.api.evaluation;

import dev.openfeature.api.types.Value;

/**
 * An API for the type-specific fetch methods offered to users.
 */
public interface EvaluationClient {

    default Boolean getBooleanValue(String key, Boolean defaultValue) {
        return getBooleanValue(key, defaultValue, EvaluationContext.EMPTY);
    }

    default Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanValue(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    default Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getBooleanDetails(key, defaultValue, ctx, options).getValue();
    }

    default FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue) {
        return getBooleanDetails(key, defaultValue, EvaluationContext.EMPTY);
    }

    default FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    FlagEvaluationDetails<Boolean> getBooleanDetails(
            String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    default String getStringValue(String key, String defaultValue) {
        return getStringValue(key, defaultValue, EvaluationContext.EMPTY);
    }

    default String getStringValue(String key, String defaultValue, EvaluationContext ctx) {
        return getStringValue(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    default String getStringValue(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getStringDetails(key, defaultValue, ctx, options).getValue();
    }

    default FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue) {
        return getStringDetails(key, defaultValue, EvaluationContext.EMPTY);
    }

    default FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return getStringDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    FlagEvaluationDetails<String> getStringDetails(
            String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    default Integer getIntegerValue(String key, Integer defaultValue) {
        return getIntegerValue(key, defaultValue, EvaluationContext.EMPTY);
    }

    default Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerValue(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    default Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getIntegerDetails(key, defaultValue, ctx, options).getValue();
    }

    default FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue) {
        return getIntegerDetails(key, defaultValue, EvaluationContext.EMPTY);
    }

    default FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    FlagEvaluationDetails<Integer> getIntegerDetails(
            String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    default Double getDoubleValue(String key, Double defaultValue) {
        return getDoubleValue(key, defaultValue, EvaluationContext.EMPTY);
    }

    default Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx) {
        return getDoubleValue(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    default Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getDoubleDetails(key, defaultValue, ctx, options).getValue();
    }

    default FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue) {
        return getDoubleDetails(key, defaultValue, EvaluationContext.EMPTY);
    }

    default FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue, EvaluationContext ctx) {
        return getDoubleDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    FlagEvaluationDetails<Double> getDoubleDetails(
            String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    default Value getObjectValue(String key, Value defaultValue) {
        return getObjectValue(key, defaultValue, EvaluationContext.EMPTY);
    }

    default Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx) {
        return getObjectValue(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    default Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getObjectDetails(key, defaultValue, ctx, options).getValue();
    }

    default FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue) {
        return getObjectDetails(key, defaultValue, EvaluationContext.EMPTY);
    }

    default FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue, EvaluationContext ctx) {
        return getObjectDetails(key, defaultValue, ctx, FlagEvaluationOptions.builder().build());
    }

    FlagEvaluationDetails<Value> getObjectDetails(
            String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
}

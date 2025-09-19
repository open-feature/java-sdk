package dev.openfeature.api.evaluation;

import dev.openfeature.api.types.Value;

/**
 * An API for the type-specific fetch methods offered to users.
 */
public interface EvaluationClient {

    Boolean getBooleanValue(String key, Boolean defaultValue);

    Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx);

    Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue);

    FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx);

    FlagEvaluationDetails<Boolean> getBooleanDetails(
            String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    String getStringValue(String key, String defaultValue);

    String getStringValue(String key, String defaultValue, EvaluationContext ctx);

    String getStringValue(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue);

    FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx);

    FlagEvaluationDetails<String> getStringDetails(
            String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    Integer getIntegerValue(String key, Integer defaultValue);

    Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx);

    Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue);

    FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx);

    FlagEvaluationDetails<Integer> getIntegerDetails(
            String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    Double getDoubleValue(String key, Double defaultValue);

    Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx);

    Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue);

    FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue, EvaluationContext ctx);

    FlagEvaluationDetails<Double> getDoubleDetails(
            String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    Value getObjectValue(String key, Value defaultValue);

    Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx);

    Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue);

    FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue, EvaluationContext ctx);

    FlagEvaluationDetails<Value> getObjectDetails(
            String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);
}

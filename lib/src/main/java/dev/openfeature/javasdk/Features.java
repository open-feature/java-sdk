package dev.openfeature.javasdk;

public interface Features {

    Boolean getBooleanValue(String key, Boolean defaultValue);
    Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx);
    Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue);
    FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx);
    FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    String getStringValue(String key, String defaultValue);
    String getStringValue(String key, String defaultValue, EvaluationContext ctx);
    String getStringValue(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue);
    FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx);
    FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    Integer getIntegerValue(String key, Integer defaultValue);
    Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx);
    Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue);
    FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx);
    FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    // TODO: Object

    // Ah, I see this is still a TODO. I think ObjectMapper might be a decent candidate for a generic object... but I'm not sure

}

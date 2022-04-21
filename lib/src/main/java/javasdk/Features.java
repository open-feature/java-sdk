package javasdk;

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

    Long getNumberValue(String key, Long defaultValue);
    Long getNumberValue(String key, Long defaultValue, EvaluationContext ctx);
    Long getNumberValue(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue);
    FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue, EvaluationContext ctx);
    FlagEvaluationDetails<Long> getNumberDetails(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options);

    // TODO: Object

}

package dev.openfeature.sdk.compat;

/**
 * Compatibility guide and utilities for migrating from OpenFeature Java SDK v1.x to v2.0.
 *
 * <p>This package provides backward compatibility for the major breaking changes introduced
 * in v2.0.0. All classes in the dev.openfeature.sdk package (except implementation classes)
 * are deprecated and will be removed in v2.1.0.
 *
 * <h2>Quick Migration Guide</h2>
 *
 * <h3>1. Update Dependencies</h3>
 * <pre>{@code
 * <!-- Update your POM -->
 * <dependency>
 *     <groupId>dev.openfeature</groupId>
 *     <artifactId>sdk</artifactId>
 *     <version>2.0.0</version>
 * </dependency>
 * }</pre>
 *
 * <h3>2. Replace Setters with Builders</h3>
 * <pre>{@code
 * // BEFORE (will throw UnsupportedOperationException)
 * ProviderEvaluation<String> eval = new ProviderEvaluation<>();
 * eval.setValue("test");
 *
 * // AFTER (works in v2.0)
 * ProviderEvaluation<String> eval = ProviderEvaluation.<String>builder()
 *     .value("test")
 *     .build();
 * }</pre>
 *
 * <h3>3. Update Imports (Gradual)</h3>
 * <pre>{@code
 * // BEFORE (deprecated in v2.0)
 * import dev.openfeature.sdk.FeatureProvider;
 * import dev.openfeature.sdk.Features;
 *
 * // AFTER (recommended in v2.0+)
 * import dev.openfeature.api.Provider;
 * import dev.openfeature.api.evaluation.EvaluationClient;
 * }</pre>
 *
 * <h3>4. Update Interface Names</h3>
 * <pre>{@code
 * // BEFORE
 * public class MyProvider implements FeatureProvider { }
 * Features client = OpenFeature.getClient();
 *
 * // AFTER
 * public class MyProvider implements Provider { }
 * EvaluationClient client = OpenFeature.getClient();
 * }</pre>
 *
 * <h2>What Works Immediately</h2>
 * <ul>
 * <li>✅ Interface implementations (with deprecation warnings)</li>
 * <li>✅ Enum and constant usage</li>
 * <li>✅ Exception throwing and catching</li>
 * <li>✅ Object construction with builders</li>
 * <li>✅ Immutable object creation</li>
 * </ul>
 *
 * <h2>What Requires Changes</h2>
 * <ul>
 * <li>❌ Setter method usage (throws UnsupportedOperationException)</li>
 * <li>❌ Mutable object patterns</li>
 * </ul>
 *
 * <h2>Timeline</h2>
 * <ul>
 * <li><strong>v2.0.0</strong>: Compatibility layer available, deprecation warnings</li>
 * <li><strong>v2.1.0</strong>: Compatibility layer removed, breaking changes</li>
 * </ul>
 *
 * <p><strong>Action Required</strong>: Migrate all deprecated usage before v2.1.0
 *
 * @since 2.0.0
 * @see <a href="https://docs.openfeature.dev/java-sdk/v2-migration">Full Migration Guide</a>
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public final class CompatibilityGuide {

    /**
     * Migration guidance URL.
     */
    public static final String MIGRATION_GUIDE_URL = "https://docs.openfeature.dev/java-sdk/v2-migration";

    /**
     * Standard migration message for unsupported operations.
     */
    public static final String IMMUTABILITY_MESSAGE =
        "This object is now immutable. Use the builder pattern instead. " +
        "See migration guide: " + MIGRATION_GUIDE_URL;

    /**
     * Check if an import path indicates deprecated compatibility usage.
     *
     * @param importPath The import path to check
     * @return true if the import uses deprecated compatibility classes
     */
    public static boolean isDeprecatedImport(String importPath) {
        return importPath != null &&
               importPath.startsWith("dev.openfeature.sdk.") &&
               !importPath.startsWith("dev.openfeature.sdk.internal.") &&
               !importPath.startsWith("dev.openfeature.sdk.providers.") &&
               !importPath.startsWith("dev.openfeature.sdk.hooks.") &&
               !importPath.contains("OpenFeature"); // Exclude OpenFeatureClient and similar
    }

    /**
     * Get the recommended replacement import for a deprecated import.
     *
     * @param deprecatedImport The deprecated import path
     * @return The recommended replacement import path
     */
    public static String getReplacementImport(String deprecatedImport) {
        if (deprecatedImport == null || !isDeprecatedImport(deprecatedImport)) {
            return deprecatedImport;
        }

        // Interface mappings
        if (deprecatedImport.equals("dev.openfeature.sdk.FeatureProvider")) {
            return "dev.openfeature.api.Provider";
        }
        if (deprecatedImport.equals("dev.openfeature.sdk.Features")) {
            return "dev.openfeature.api.evaluation.EvaluationClient";
        }
        if (deprecatedImport.equals("dev.openfeature.sdk.Client")) {
            return "dev.openfeature.api.Client";
        }

        // POJOs and types
        if (deprecatedImport.startsWith("dev.openfeature.sdk.exceptions.")) {
            return deprecatedImport.replace("dev.openfeature.sdk.exceptions.", "dev.openfeature.api.exceptions.");
        }

        // Evaluation types
        String[] evaluationTypes = {
            "ProviderEvaluation", "FlagEvaluationDetails", "EvaluationContext",
            "MutableContext", "ImmutableContext", "BaseEvaluation"
        };
        for (String type : evaluationTypes) {
            if (deprecatedImport.equals("dev.openfeature.sdk." + type)) {
                return "dev.openfeature.api.evaluation." + type;
            }
        }

        // Type system
        String[] typeSystemTypes = {
            "Value", "Structure", "AbstractStructure", "MutableStructure",
            "ImmutableStructure", "Metadata", "ImmutableMetadata", "ClientMetadata"
        };
        for (String type : typeSystemTypes) {
            if (deprecatedImport.equals("dev.openfeature.sdk." + type)) {
                return "dev.openfeature.api.types." + type;
            }
        }

        // Events
        if (deprecatedImport.equals("dev.openfeature.sdk.EventBus")) {
            return "dev.openfeature.api.events.EventBus";
        }

        // Hooks
        String[] hookTypes = {"Hook", "BooleanHook", "StringHook", "IntegerHook", "DoubleHook"};
        for (String type : hookTypes) {
            if (deprecatedImport.equals("dev.openfeature.sdk." + type)) {
                return "dev.openfeature.api.lifecycle." + type;
            }
        }

        // Tracking
        if (deprecatedImport.equals("dev.openfeature.sdk.Tracking")) {
            return "dev.openfeature.api.tracking.Tracking";
        }

        // Core types
        String[] coreTypes = {
            "ErrorCode", "Reason", "FlagValueType", "ProviderState", "ProviderEvent",
            "Telemetry", "TransactionContextPropagator", "Awaitable"
        };
        for (String type : coreTypes) {
            if (deprecatedImport.equals("dev.openfeature.sdk." + type)) {
                return "dev.openfeature.api." + type;
            }
        }

        return deprecatedImport; // Return unchanged if no mapping found
    }

    private CompatibilityGuide() {
        // Utility class
    }
}
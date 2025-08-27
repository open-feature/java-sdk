package dev.openfeature.sdk.testutils;

import static dev.openfeature.api.Structure.mapToStructure;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.api.ImmutableMetadata;
import dev.openfeature.api.Value;
import dev.openfeature.sdk.providers.memory.Flag;
import java.util.HashMap;
import java.util.Map;
/**
 * Test flags utils.
 */
public class TestFlagsUtils {

    private TestFlagsUtils() {
        // Utility class
    }

    public static final String BOOLEAN_FLAG_KEY = "boolean-flag";
    public static final String STRING_FLAG_KEY = "string-flag";
    public static final String INT_FLAG_KEY = "integer-flag";
    public static final String FLOAT_FLAG_KEY = "float-flag";
    public static final String OBJECT_FLAG_KEY = "object-flag";
    public static final String CONTEXT_AWARE_FLAG_KEY = "context-aware";
    public static final String WRONG_FLAG_KEY = "wrong-flag";
    public static final String METADATA_FLAG_KEY = "metadata-flag";

    /**
     * Building flags for testing purposes.
     *
     * @return map of flags
     */
    public static Map<String, Flag<?>> buildFlags() {
        Map<String, Flag<?>> flags = new HashMap<>();
        flags.put(
                BOOLEAN_FLAG_KEY,
                Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("on")
                        .build());
        flags.put(
                STRING_FLAG_KEY,
                Flag.builder()
                        .variant("greeting", "hi")
                        .variant("parting", "bye")
                        .defaultVariant("greeting")
                        .build());
        flags.put(
                INT_FLAG_KEY,
                Flag.builder()
                        .variant("one", 1)
                        .variant("ten", 10)
                        .defaultVariant("ten")
                        .build());
        flags.put(
                FLOAT_FLAG_KEY,
                Flag.builder()
                        .variant("tenth", 0.1)
                        .variant("half", 0.5)
                        .defaultVariant("half")
                        .build());
        flags.put(
                OBJECT_FLAG_KEY,
                Flag.builder()
                        .variant("empty", new HashMap<>())
                        .variant(
                                "template",
                                new Value(mapToStructure(ImmutableMap.of(
                                        "showImages", new Value(true),
                                        "title", new Value("Check out these pics!"),
                                        "imagesPerPage", new Value(100)))))
                        .defaultVariant("template")
                        .build());
        flags.put(
                CONTEXT_AWARE_FLAG_KEY,
                Flag.<String>builder()
                        .variant("internal", "INTERNAL")
                        .variant("external", "EXTERNAL")
                        .defaultVariant("external")
                        .contextEvaluator((flag, evaluationContext) -> {
                            if (new Value(false).equals(evaluationContext.getValue("customer"))) {
                                return (String) flag.getVariants().get("internal");
                            } else {
                                return (String) flag.getVariants().get(flag.getDefaultVariant());
                            }
                        })
                        .build());
        flags.put(
                WRONG_FLAG_KEY,
                Flag.builder()
                        .variant("one", "uno")
                        .variant("two", "dos")
                        .defaultVariant("one")
                        .build());
        flags.put(
                METADATA_FLAG_KEY,
                Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("on")
                        .flagMetadata(ImmutableMetadata.builder()
                                .addString("string", "1.0.2")
                                .addInteger("integer", 2)
                                .addBoolean("boolean", true)
                                .addDouble("float", 0.1d)
                                .build())
                        .build());
        return flags;
    }
}

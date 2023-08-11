package dev.openfeature.sdk.testutils;

import com.google.common.collect.ImmutableMap;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.providers.memory.Flag;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

import static dev.openfeature.sdk.MutableStructure.mapToStructure;

/**
 * Test flags utils.
 */
@UtilityClass
public class TestFlagsUtils {

    /**
     * Building flags for testing purposes.
     * @return
     */
    public static Map<String, Flag<?>> buildFlags() {
        Map<String, Flag<?>> flags = new HashMap<>();
        flags.put("boolean-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("on", true)
                .variant("off", false)
                .defaultVariant("on")
                .build());
        flags.put("string-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("greeting", "hi")
                .variant("parting", "bye")
                .defaultVariant("greeting")
                .build());
        flags.put("integer-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("one", 1)
                .variant("ten", 10)
                .defaultVariant("ten")
                .build());
        flags.put("float-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("tenth", 0.1)
                .variant("half", 0.5)
                .defaultVariant("half")
                .build());
        flags.put("object-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("empty", new HashMap<>())
                .variant("template", new Value(mapToStructure(ImmutableMap.of(
                        "showImages", new Value(true),
                        "title", new Value("Check out these pics!"),
                        "imagesPerPage", new Value(100)
                ))))
                .defaultVariant("template")
                .build());
        flags.put("context-aware", Flag.<String>builder().state(Flag.State.ENABLED)
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
        flags.put("wrong-flag", Flag.builder().state(Flag.State.ENABLED)
                .variant("one", "uno")
                .variant("two", "dos")
                .defaultVariant("one")
                .build());
        return flags;
    }
}

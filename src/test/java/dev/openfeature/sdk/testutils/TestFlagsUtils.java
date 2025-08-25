package dev.openfeature.sdk.testutils;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.openfeature.sdk.ImmutableMetadata;
import dev.openfeature.sdk.providers.memory.ContextEvaluator;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.testutils.jackson.ContextEvaluatorDeserializer;
import dev.openfeature.sdk.testutils.jackson.ImmutableMetadataDeserializer;
import dev.openfeature.sdk.testutils.jackson.InMemoryFlagMixin;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Test flags utils.
 */
@Slf4j
@UtilityClass
public class TestFlagsUtils {

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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature(), true);
        objectMapper.addMixIn(Flag.class, InMemoryFlagMixin.class);
        objectMapper.addMixIn(Flag.FlagBuilder.class, InMemoryFlagMixin.FlagBuilderMixin.class);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(ImmutableMetadata.class, new ImmutableMetadataDeserializer());
        module.addDeserializer(ContextEvaluator.class, new ContextEvaluatorDeserializer());
        objectMapper.registerModule(module);

        Map<String, Flag<?>> flagsJson;
        try {
            flagsJson = objectMapper.readValue(
                    Paths.get("spec/specification/assets/gherkin/test-flags.json")
                            .toFile(),
                    new TypeReference<>() {});

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return flagsJson;
    }
}

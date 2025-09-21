package dev.openfeature.sdk.providers.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.ImmutableMetadata;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FlagTest {

    @Test
    void builder_shouldCreateFlagWithVariants() {
        Map<String, Object> variants = Map.of("on", true, "off", false);
        
        Flag<Boolean> flag = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .build();

        assertEquals(variants, flag.getVariants());
        assertEquals("on", flag.getDefaultVariant());
    }

    @Test
    void builder_shouldCreateFlagWithIndividualVariants() {
        Flag<String> flag = Flag.<String>builder()
                .variant("greeting", "hello")
                .variant("farewell", "goodbye")
                .defaultVariant("greeting")
                .build();

        Map<String, Object> expectedVariants = Map.of("greeting", "hello", "farewell", "goodbye");
        assertEquals(expectedVariants, flag.getVariants());
        assertEquals("greeting", flag.getDefaultVariant());
    }

    @Test
    void builder_shouldCreateFlagWithContextEvaluator() {
        ContextEvaluator<String> evaluator = (flag, ctx) -> "evaluated";
        
        Flag<String> flag = Flag.<String>builder()
                .variant("default", "value")
                .defaultVariant("default")
                .contextEvaluator(evaluator)
                .build();

        assertEquals(evaluator, flag.getContextEvaluator());
    }

    @Test
    void builder_shouldCreateFlagWithMetadata() {
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addString("description", "Test flag")
                .build();
        
        Flag<Boolean> flag = Flag.<Boolean>builder()
                .variant("on", true)
                .defaultVariant("on")
                .flagMetadata(metadata)
                .build();

        assertEquals(metadata, flag.getFlagMetadata());
    }

    @Test
    void builder_shouldOverwriteVariantsMap() {
        Map<String, Object> initialVariants = Map.of("old", "value");
        Map<String, Object> newVariants = Map.of("new", "value");
        
        Flag<String> flag = Flag.<String>builder()
                .variant("manual", "added")
                .variants(initialVariants)
                .variants(newVariants)
                .defaultVariant("new")
                .build();

        assertEquals(newVariants, flag.getVariants());
        assertFalse(flag.getVariants().containsKey("manual"));
        assertFalse(flag.getVariants().containsKey("old"));
    }

    @Test
    void equals_shouldReturnTrueForIdenticalFlags() {
        Map<String, Object> variants = Map.of("on", true, "off", false);
        ImmutableMetadata metadata = ImmutableMetadata.builder().addString("desc", "test").build();
        ContextEvaluator<Boolean> evaluator = (flag, ctx) -> true;

        Flag<Boolean> flag1 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .contextEvaluator(evaluator)
                .flagMetadata(metadata)
                .build();

        Flag<Boolean> flag2 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .contextEvaluator(evaluator)
                .flagMetadata(metadata)
                .build();

        assertEquals(flag1, flag2);
        assertEquals(flag2, flag1);
    }

    @Test
    void equals_shouldReturnFalseForDifferentVariants() {
        Flag<Boolean> flag1 = Flag.<Boolean>builder()
                .variant("on", true)
                .defaultVariant("on")
                .build();

        Flag<Boolean> flag2 = Flag.<Boolean>builder()
                .variant("off", false)
                .defaultVariant("off")
                .build();

        assertNotEquals(flag1, flag2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentDefaultVariant() {
        Map<String, Object> variants = Map.of("on", true, "off", false);
        
        Flag<Boolean> flag1 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .build();

        Flag<Boolean> flag2 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("off")
                .build();

        assertNotEquals(flag1, flag2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentContextEvaluator() {
        Map<String, Object> variants = Map.of("on", true);
        ContextEvaluator<Boolean> evaluator1 = (flag, ctx) -> true;
        ContextEvaluator<Boolean> evaluator2 = (flag, ctx) -> false;

        Flag<Boolean> flag1 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .contextEvaluator(evaluator1)
                .build();

        Flag<Boolean> flag2 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .contextEvaluator(evaluator2)
                .build();

        assertNotEquals(flag1, flag2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentMetadata() {
        Map<String, Object> variants = Map.of("on", true);
        ImmutableMetadata metadata1 = ImmutableMetadata.builder().addString("desc", "first").build();
        ImmutableMetadata metadata2 = ImmutableMetadata.builder().addString("desc", "second").build();

        Flag<Boolean> flag1 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .flagMetadata(metadata1)
                .build();

        Flag<Boolean> flag2 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .flagMetadata(metadata2)
                .build();

        assertNotEquals(flag1, flag2);
    }

    @Test
    void equals_shouldHandleSelfEquality() {
        Flag<Boolean> flag = Flag.<Boolean>builder()
                .variant("on", true)
                .defaultVariant("on")
                .build();

        assertEquals(flag, flag);
    }

    @Test
    void equals_shouldHandleNullAndDifferentClass() {
        Flag<Boolean> flag = Flag.<Boolean>builder()
                .variant("on", true)
                .defaultVariant("on")
                .build();

        assertNotEquals(flag, null);
        assertNotEquals(flag, "not a flag");
        assertNotEquals(flag, new Object());
    }

    @Test
    void hashCode_shouldBeConsistentWithEquals() {
        Map<String, Object> variants = Map.of("on", true, "off", false);
        ImmutableMetadata metadata = ImmutableMetadata.builder().addString("desc", "test").build();
        ContextEvaluator<Boolean> evaluator = (flag, ctx) -> true;

        Flag<Boolean> flag1 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .contextEvaluator(evaluator)
                .flagMetadata(metadata)
                .build();

        Flag<Boolean> flag2 = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .contextEvaluator(evaluator)
                .flagMetadata(metadata)
                .build();

        assertEquals(flag1.hashCode(), flag2.hashCode());
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentFlags() {
        Flag<Boolean> flag1 = Flag.<Boolean>builder()
                .variant("on", true)
                .defaultVariant("on")
                .build();

        Flag<Boolean> flag2 = Flag.<Boolean>builder()
                .variant("off", false)
                .defaultVariant("off")
                .build();

        assertNotEquals(flag1.hashCode(), flag2.hashCode());
    }

    @Test
    void toString_shouldIncludeAllFields() {
        Map<String, Object> variants = Map.of("on", true, "off", false);
        ImmutableMetadata metadata = ImmutableMetadata.builder().addString("desc", "test").build();
        ContextEvaluator<Boolean> evaluator = (flag, ctx) -> true;

        Flag<Boolean> flag = Flag.<Boolean>builder()
                .variants(variants)
                .defaultVariant("on")
                .contextEvaluator(evaluator)
                .flagMetadata(metadata)
                .build();

        String toStringResult = flag.toString();
        assertTrue(toStringResult.contains("Flag{"));
        assertTrue(toStringResult.contains("variants="));
        assertTrue(toStringResult.contains("defaultVariant="));
        assertTrue(toStringResult.contains("contextEvaluator="));
        assertTrue(toStringResult.contains("flagMetadata="));
        assertTrue(toStringResult.contains("on"));
        assertTrue(toStringResult.contains("true"));
        assertTrue(toStringResult.contains("false"));
    }

    @Test
    void builder_shouldCreateEmptyFlag() {
        Flag<String> flag = Flag.<String>builder().build();

        assertTrue(flag.getVariants().isEmpty());
        assertEquals(null, flag.getDefaultVariant());
        assertEquals(null, flag.getContextEvaluator());
        assertEquals(null, flag.getFlagMetadata());
    }

    @Test
    void builder_shouldChainMethodCalls() {
        ImmutableMetadata metadata = ImmutableMetadata.builder().addString("test", "value").build();
        ContextEvaluator<Integer> evaluator = (flag, ctx) -> 42;

        Flag<Integer> flag = Flag.<Integer>builder()
                .variant("low", 1)
                .variant("high", 100)
                .defaultVariant("low")
                .contextEvaluator(evaluator)
                .flagMetadata(metadata)
                .build();

        Map<String, Object> expectedVariants = Map.of("low", 1, "high", 100);
        assertEquals(expectedVariants, flag.getVariants());
        assertEquals("low", flag.getDefaultVariant());
        assertEquals(evaluator, flag.getContextEvaluator());
        assertEquals(metadata, flag.getFlagMetadata());
    }

    @Test
    void builder_variantsMap_shouldReplaceExistingVariants() {
        Map<String, Object> newVariants = new HashMap<>();
        newVariants.put("new", "value");

        Flag<String> flag = Flag.<String>builder()
                .variants(newVariants)
                .defaultVariant("new")
                .build();

        assertEquals(newVariants, flag.getVariants());
        assertTrue(flag.getVariants().containsKey("new"));
        assertEquals("value", flag.getVariants().get("new"));
    }
}
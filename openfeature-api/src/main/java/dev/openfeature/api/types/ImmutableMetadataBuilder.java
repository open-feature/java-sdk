package dev.openfeature.api.types;

/**
 * Immutable builder for {@link Metadata}.
 */
public interface ImmutableMetadataBuilder {
    ImmutableMetadataBuilder add(String key, String value);

    ImmutableMetadataBuilder add(String key, Integer value);

    ImmutableMetadataBuilder add(String key, Long value);

    ImmutableMetadataBuilder add(String key, Float value);

    ImmutableMetadataBuilder add(String key, Double value);

    ImmutableMetadataBuilder add(String key, Boolean value);

    Metadata build();
}

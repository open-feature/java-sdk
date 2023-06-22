package dev.openfeature.sdk;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
@Builder
@Jacksonized
public class ProviderEvaluation<T> implements BaseEvaluation<T> {
    T value;
    @Nullable String variant;
    @Nullable private String reason;
    ErrorCode errorCode;
    @Nullable private String errorMessage;
    @Builder.Default
    private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();
}

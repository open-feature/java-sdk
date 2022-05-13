package dev.openfeature.javasdk;

import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;

@Data @Builder
public class ProviderEvaluation<T> implements BaseEvaluation<T> {
    T value;
    @Nullable String variant;
    Reason reason;
    @Nullable ErrorCode errorCode;
}

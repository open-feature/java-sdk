package javasdk;

import lombok.Data;

import javax.annotation.Nullable;

@Data
public class ProviderEvaluation<T> {
    final T value;
    @Nullable String variant;
    final Reason reason;
    @Nullable ErrorCode errorCode;
}

package javasdk;

public interface BaseEvaluation<T> {
    T getValue();

    String getVariant();

    Reason getReason();

    ErrorCode getErrorCode();
}

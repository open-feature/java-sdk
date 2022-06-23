package dev.openfeature.javasdk;

public class NotImplementedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotImplementedException(String message){
        super(message);
    }
}

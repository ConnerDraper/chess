package service.exception;

public class AlreadyTakenException extends Exception {
    public AlreadyTakenException(String errorMessage) {
        super(errorMessage);
    }
}

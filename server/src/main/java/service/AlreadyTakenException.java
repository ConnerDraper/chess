package service;

public class AlreadyTakenException extends Exception {
    public AlreadyTakenException(String errorMessage) {
        super(errorMessage);
    }
}

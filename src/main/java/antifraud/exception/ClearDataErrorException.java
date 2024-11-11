package antifraud.exception;

public class ClearDataErrorException extends RuntimeException{
    public ClearDataErrorException() {
        super("Error clearing database");
    }
}

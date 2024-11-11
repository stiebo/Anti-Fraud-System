package antifraud.exception;

public class StolenCardExistsException extends RuntimeException{
    public StolenCardExistsException() {
        super("Stole card already exists");
    }
}

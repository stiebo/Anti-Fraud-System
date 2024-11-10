package antifraud.exception;

public class UnableToLockAdminException extends RuntimeException {
    public UnableToLockAdminException() {
        super("Cannot lock admin");
    }
}

package antifraud.exception;

public class SuspiciousIpExistsException extends RuntimeException{
    public SuspiciousIpExistsException() {
        super("Suspicious IP already exists");
    }
}

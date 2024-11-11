package antifraud.exception;

public class SuspiciousIpNotFoundException extends RuntimeException{
    public SuspiciousIpNotFoundException() {
        super("Suspicious IP not found");
    }
}

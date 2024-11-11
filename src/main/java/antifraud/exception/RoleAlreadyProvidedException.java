package antifraud.exception;

public class RoleAlreadyProvidedException extends RuntimeException{
    public RoleAlreadyProvidedException() {
        super("Role already provided");
    }
}

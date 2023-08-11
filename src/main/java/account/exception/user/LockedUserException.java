package account.exception.user;

public class LockedUserException extends RuntimeException{

    public LockedUserException(){
        super("User account is locked");
    }
}

package account.exception.password;

public class MatchingPasswordException extends RuntimeException {
    public MatchingPasswordException(){
        super("The passwords must be different!");
    }
}

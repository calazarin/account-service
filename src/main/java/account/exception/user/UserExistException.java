package account.exception.user;

public class UserExistException extends RuntimeException {

    public UserExistException(){
        super("User exist!");
    }

}

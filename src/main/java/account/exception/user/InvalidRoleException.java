package account.exception.user;

public class InvalidRoleException extends RuntimeException{

    public InvalidRoleException(String msg){
        super(msg);
    }
}

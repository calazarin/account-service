package account.exception.user;

public class RoleNotFoundException extends RuntimeException{

    public RoleNotFoundException(){
        super("Role not found!");
    }

}

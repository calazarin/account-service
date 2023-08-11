package account.exception.user;

import org.springframework.http.HttpStatus;

import java.util.Optional;

public class InvalidUserActionException extends RuntimeException{

    private Optional<HttpStatus> httpStatusOptional;

    public  Optional<HttpStatus> getHttpStatus(){
        return httpStatusOptional;
    }

    public InvalidUserActionException(String msg){
        super(msg);
        this.httpStatusOptional = Optional.empty();
    }

    public InvalidUserActionException(String msg, HttpStatus customResponseCode){
        super(msg);
        this.httpStatusOptional = Optional.of(customResponseCode);
    }
}

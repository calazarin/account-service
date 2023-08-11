package account.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
public class ApiError {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    @JsonInclude(JsonInclude.Include. NON_NULL)
    private String message;
    private String path;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    public ApiError(HttpStatus status) {
        this();
        this.status = status.value();
        this.message = "Unexpected error";
    }

    public ApiError(HttpStatus status, String message) {
        this();
        this.status = status.value();
        this.message = message;
    }

    public ApiError(HttpStatus status, Optional<String> messageOpt, String path) {
        this();
        this.status = status.value();
        if(messageOpt.isPresent()){
            this.message = messageOpt.get();
        }
        this.error = status.getReasonPhrase();
        this.path = onlyPath(path);
    }

    public static String onlyPath(String fullPath){
        if(fullPath.contains("uri=")){
            return fullPath.split("=")[1];
        }
        return fullPath;
    }
}

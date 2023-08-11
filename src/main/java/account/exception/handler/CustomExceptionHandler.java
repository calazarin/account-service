package account.exception.handler;

import account.exception.ApiError;
import account.exception.password.BreachedPasswordException;
import account.exception.password.MatchingPasswordException;
import account.exception.payment.InvalidPaymentException;
import account.exception.payment.PaymentDoesNotExistException;
import account.exception.user.InvalidRoleException;
import account.exception.user.InvalidUserActionException;
import account.exception.user.RoleNotFoundException;
import account.exception.user.UserExistException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String errMsg = ex.getBindingResult().getAllErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));

        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST,
                Optional.of(errMsg), request.getDescription(false)));
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolationException(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    @ExceptionHandler(value = {UserExistException.class,
            MatchingPasswordException.class,
            BreachedPasswordException.class,
            InvalidPaymentException.class,
            PaymentDoesNotExistException.class,
            InvalidUserActionException.class,
            InvalidRoleException.class
    })
    protected ResponseEntity<Object> handleBusinessExceptions(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if(ex instanceof InvalidUserActionException) {
            Optional<HttpStatus> httpStatusOpt = ((InvalidUserActionException)ex).getHttpStatus();
            if(httpStatusOpt.isPresent()){
                status = httpStatusOpt.get();
            }
        }

        return buildResponseEntity(new ApiError(status, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, HttpStatus.valueOf(apiError.getStatus()));
    }

    @ExceptionHandler({AuthenticationException.class})
    @ResponseBody
    public ResponseEntity<Object> handleAuthenticationException(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.UNAUTHORIZED, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    @ExceptionHandler({account.exception.user.LockedUserException.class})
    @ResponseBody
    public ResponseEntity<Object> handleLockedUserException(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.UNAUTHORIZED, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseBody
    public ResponseEntity<Object> handleAccessDeniedException(RuntimeException ex, WebRequest request) {
        log.error("AccessDeniedException!!");
        return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, Optional.of("Access Denied!"),
                request.getDescription(false)));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Object> handleRoleNotFoundException(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, Optional.of(ex.getMessage()),
                request.getDescription(false)));
    }

}

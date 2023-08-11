package account.security;

import account.entity.AppUser;
import account.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationEvents {

    private final UserService userService;

    @Autowired
    public AuthenticationEvents(UserService userService){
        this.userService = userService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        AppUser userDetails = (AppUser) success.getAuthentication().getPrincipal();
        log.info("Called login onSuccess listener {}", userDetails.getUsername());
        userService.resetFailedLoginAttempts(userDetails);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        if(failures.getException() instanceof BadCredentialsException){
            String failedUsername = (String) ((UsernamePasswordAuthenticationToken)failures.getSource()).getPrincipal();
            userService.handleFailedLogin(failedUsername);
        }
    }
}
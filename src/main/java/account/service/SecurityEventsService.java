package account.service;

import account.entity.SecurityEvent;
import account.enums.SecureEventActionEnum;
import account.exception.ApiError;
import account.repository.SecurityEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;
import java.util.List;

import static account.controller.UserController.ADMIN_USER_DELETE;
import static account.controller.UserController.ADMIN_USER_ROLE;
import static account.controller.UserController.AUHT_SIGNUP_URL;
import static account.controller.UserController.AUTH_CHANGE_PASS_URL;

@Slf4j
@Service
public class SecurityEventsService {

    private final SecurityEventRepository securityEventRepository;
    private final WebRequest webRequest;

    @Autowired
    public SecurityEventsService(SecurityEventRepository securityEventRepository, WebRequest webRequest) {
        this.securityEventRepository = securityEventRepository;
        this.webRequest = webRequest;
    }

    public List<SecurityEvent> getAllSecurityEvents() {
        return securityEventRepository.findAll();
    }

    public void recordLoginFailedEvent(String usernameTryingToLogin){
        String webRequestDesc = webRequest.getDescription(false);
        recordSecurityEvent(SecureEventActionEnum.LOGIN_FAILED,
                usernameTryingToLogin.toLowerCase(),
                ApiError.onlyPath(webRequestDesc),
                ApiError.onlyPath(webRequestDesc));
    }

    public void recordSecurityEvent(SecureEventActionEnum action, String subject,
                                    String object, String path){

        SecurityEvent securityEvent = new SecurityEvent(action.getName(), subject, object, path);
        log.info("Adding a new security event: {}", securityEvent);
        securityEventRepository.save(securityEvent);
    }

    public void recordSecurityEvent(SecurityEvent securityEvent){
        log.info("Adding a new security event: {}", securityEvent);
        securityEventRepository.save(securityEvent);
    }

    public void recordSecurityEvents(List<SecurityEvent> securityEvents){
        securityEventRepository.saveAll(securityEvents);
    }

    public static SecurityEvent grantRoleEvent(String subject, String role, String object){
        return new SecurityEvent(SecureEventActionEnum.GRANT_ROLE.getName(),
                subject.toLowerCase(),
                String.format("Grant role %s to %s", role.toUpperCase(),
                object.toLowerCase()),ADMIN_USER_ROLE);
    }

    public static SecurityEvent changePasswordEvent(String subject){
        return new SecurityEvent(SecureEventActionEnum.CHANGE_PASSWORD.getName(),
                subject.toLowerCase(),
                subject.toLowerCase(),
                AUTH_CHANGE_PASS_URL);
    }

    public static SecurityEvent createUserEvent(String object){
        return new SecurityEvent(SecureEventActionEnum.CREATE_USER.getName(),
                "Anonymous",
                object.toLowerCase(),
                AUHT_SIGNUP_URL);
    }

    public static SecurityEvent deleteUserEvent(String subject, String object){
        return new SecurityEvent(SecureEventActionEnum.DELETE_USER.getName(),
                subject.toLowerCase(),
                object.toLowerCase(),
                ADMIN_USER_DELETE);
    }

    public static SecurityEvent removeRoleEvent(String subject, String role, String object){
        return new SecurityEvent(SecureEventActionEnum.REMOVE_ROLE.getName(),
                subject.toLowerCase(),
                String.format("Remove role %s from %s", role.toUpperCase(),
                        object.toLowerCase()),ADMIN_USER_ROLE);
    }

    public void recordLockUserEvent(String subject, String object){
        String webRequestDesc = webRequest.getDescription(false);

        SecurityEvent securityEvent = new SecurityEvent(
                SecureEventActionEnum.LOCK_USER.getName(),
                subject.toLowerCase(),
                String.format("Lock user %s",object.toLowerCase()),
                ApiError.onlyPath(webRequestDesc));

        recordSecurityEvent(securityEvent);
    }

    public void recordUnlockUserEvent(String subject, String object){
        String webRequestDesc = webRequest.getDescription(false);

        SecurityEvent securityEvent = new SecurityEvent(
                SecureEventActionEnum.UNLOCK_USER.getName(),
                subject.toLowerCase(),
                String.format("Unlock user %s",object.toLowerCase()),
                ApiError.onlyPath(webRequestDesc));

        recordSecurityEvent(securityEvent);
    }

    public void recordAccessDeniedEvent(String subject){
        String webRequestDesc = webRequest.getDescription(false);

        SecurityEvent securityEvent = new SecurityEvent(
                SecureEventActionEnum.ACCESS_DENIED.getName(),
                subject.toLowerCase(),
                ApiError.onlyPath(webRequestDesc),
                ApiError.onlyPath(webRequestDesc));

        recordSecurityEvent(securityEvent);
    }

    public void recordBruteForceEvent(String subject){
        String webRequestDesc = webRequest.getDescription(false);

        SecurityEvent securityEvent = new SecurityEvent(
                SecureEventActionEnum.BRUTE_FORCE.getName(),
                subject.toLowerCase(),
                ApiError.onlyPath(webRequestDesc),
                ApiError.onlyPath(webRequestDesc));

        recordSecurityEvent(securityEvent);
    }

}

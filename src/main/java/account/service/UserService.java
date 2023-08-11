package account.service;

import account.entity.AppUser;
import account.entity.Role;
import account.entity.SecurityEvent;
import account.enums.AccessActionEnum;
import account.enums.RoleActionEnum;
import account.enums.UserRoleEnum;
import account.exception.user.InvalidRoleException;
import account.exception.user.InvalidUserActionException;
import account.exception.password.BreachedPasswordException;
import account.exception.password.MatchingPasswordException;
import account.exception.user.RoleNotFoundException;
import account.exception.user.UserExistException;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static account.enums.RoleActionEnum.GRANT;
import static account.enums.UserRoleEnum.ADMINISTRATOR;
import static account.service.SecurityEventsService.changePasswordEvent;
import static account.service.SecurityEventsService.createUserEvent;
import static account.service.SecurityEventsService.deleteUserEvent;
import static account.service.SecurityEventsService.grantRoleEvent;
import static account.service.SecurityEventsService.removeRoleEvent;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityEventsService securityEventsService;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       SecurityEventsService securityEventsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityEventsService = securityEventsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<AppUser> user = userRepository.findByUsernameIgnoreCase(username);
        if (user.isPresent()){
            AppUser retrievedUser = user.get();
            log.info("Loaded user {} with failed attempts {}, account non locked {} and roles {}", username,
                    retrievedUser.getFailedAttempt(), retrievedUser.isAccountNonLocked(),
                    user.get().getRoles().stream().map(Role::getName).collect(Collectors.toList()));

            return retrievedUser;
        } else{
            securityEventsService.recordLoginFailedEvent(username);
            log.error("Trying to load user by username; not found {}", username);
            throw new UsernameNotFoundException(String.format("Username[%s] not found"));
        }
    }

    public void handleFailedLogin(String failedUsername) {

        final int MAX_LOGIN_ATTEMPTS = 5;
        log.debug("Login failed; logging a new security event and increasing failed attempted");

        Optional<AppUser> appUserOpt = userRepository.findByUsernameIgnoreCase(failedUsername);
        if(appUserOpt.isPresent()){
            AppUser user = appUserOpt.get();

            user.setFailedAttempt(user.getFailedAttempt() + 1);
            log.debug("Failed attempts for user {} now is {}", user.getUsername(), user.getFailedAttempt());

            userRepository.save(user);
            securityEventsService.recordLoginFailedEvent(failedUsername);

            if(user.getFailedAttempt() >= MAX_LOGIN_ATTEMPTS && !isAdministrator(user)){

                log.error("User {} is blocked due too much attempts!", failedUsername);
                securityEventsService.recordBruteForceEvent(failedUsername);

                user.setAccountNonLocked(false);

                securityEventsService.recordLockUserEvent(failedUsername, failedUsername);
                userRepository.save(user);

                throw new LockedException("User account is locked");
            }

        }
    }

    private boolean isAdministrator(AppUser user){
        return user.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(ADMINISTRATOR.getName()));
    }

    public void resetFailedLoginAttempts(AppUser userDetails){
        if(userDetails.getFailedAttempt() > 0) {
            userDetails.setFailedAttempt(0);
            userRepository.save(userDetails);
        }
    }

    public AppUser registerNewUser(String name, String lastName, String email, String password){

        if(breachedPasswords().contains(password)){
            throw new BreachedPasswordException();
        }

        log.debug("Signing up new user with e-mai {}", email);

        List<AppUser> allUsers = userRepository.findAll();
        blockDuplicatedUsers(email, allUsers);

        AppUser newUser = new AppUser(name, lastName, email, passwordEncoder.encode(password));

        if(allUsers.isEmpty()){
            newUser.setRoles(Arrays.asList(roleRepository.findByNameIgnoreCase(ADMINISTRATOR.getName()).get()));
            log.debug("First user, setting its role to ADMINISTRATOR! user is {}", email);
        } else {
            newUser.setRoles(Arrays.asList( roleRepository.findByNameIgnoreCase(UserRoleEnum.USER.getName()).get()));
            log.debug("Not the first user, setting its role to USER! user is {}", email);
        }

        log.info("Registering a new user [name={}, lastName={}, email={}]", name, lastName, email);
        AppUser createdUser = this.userRepository.save(newUser);
        securityEventsService.recordSecurityEvent(createUserEvent(email));
        return createdUser;
    }

    private void blockDuplicatedUsers(String userTobeAddedEmail, List<AppUser> allUsers){

        if(allUsers.size() > 1 && allUsers.stream().anyMatch(usr -> usr.getUsername().equalsIgnoreCase(userTobeAddedEmail))){
            log.error("User {} already exists!", userTobeAddedEmail);
            throw new UserExistException();
        }
    }

    public void updateUserPassword(String userEmail, String newPassword){

        Optional<AppUser> user = findUserAndThrowExceptionIfDoesExist(userEmail);

        AppUser userToBeUpdated = user.get();
        if(passwordEncoder.matches(newPassword, userToBeUpdated.getPassword())){
            throw new MatchingPasswordException();
        }

        if(breachedPasswords().contains(newPassword)){
            throw new BreachedPasswordException();
        }

        userToBeUpdated.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userToBeUpdated);

        securityEventsService.recordSecurityEvent(changePasswordEvent(userEmail));
    }

    private List<String> breachedPasswords(){
        return Arrays.asList(
                "PasswordForJanuary",
                "PasswordForFebruary",
                "PasswordForMarch",
                "PasswordForApril",
                "PasswordForMay",
                "PasswordForJune",
                "PasswordForJuly",
                "PasswordForAugust",
                "PasswordForSeptember",
                "PasswordForOctober",
                "PasswordForNovember",
                "PasswordForDecember"
        );
    }

    private Optional<AppUser> findUserAndThrowExceptionIfDoesExist(String username){
        Optional<AppUser> userOpt = userRepository.findByUsernameIgnoreCase(username);

        if(!userOpt.isPresent()){
            log.error("User does exist!", username);
            throw new InvalidUserActionException("User not found!", HttpStatus.NOT_FOUND);
        }

        return userOpt;
    }

    public void deleteUser(String loggedInUser, String userToDeleteEmail){

        Optional<AppUser> userOpt = findUserAndThrowExceptionIfDoesExist(userToDeleteEmail);

        AppUser userToBeDeleted = userOpt.get();

        log.debug("User to be deleted: {}; roles: {}", userToBeDeleted.getUsername(), userToBeDeleted.getRoles().stream()
                        .map(r -> r.getName()).collect(Collectors.toList()));

        if(loggedInUser.equalsIgnoreCase(userToDeleteEmail) && isAdministrator(userToBeDeleted)){
            log.error("Admin user cannot delete itself! Admin user e-mail is  {}", userToBeDeleted.getUsername());
            throw new InvalidUserActionException("Can't remove ADMINISTRATOR role!");
        }

        userRepository.delete(userToBeDeleted);
        securityEventsService.recordSecurityEvent(deleteUserEvent(loggedInUser, userToDeleteEmail));
    }


    private String[] validateRolesOperations(String operation){

        if(operation.isEmpty()){
            throw new InvalidUserActionException("A role operation must be provided!");
        } else {

            final List<String> validOperations = Arrays.stream(RoleActionEnum.values())
                    .map(RoleActionEnum::getAction).toList();

            if (operation.contains(",")) {
                String[] operations = operation.split(",");
                boolean isValid = Arrays.stream(operations).allMatch(op -> validOperations.contains(op));
                if (!isValid) {
                    logInvalidOperationAndThrowException(Arrays.toString(operations));
                }
            } else {
                if (!validOperations.contains(operation)) {
                    logInvalidOperationAndThrowException(operation);
                }
                return new String[] {operation};
            }
        }

        return new String[]{};
    }

    private void logInvalidOperationAndThrowException(String providedOperations){
        log.error("Invalid role operation(s) provided {}", providedOperations);
        throw new InvalidUserActionException("Invalid role operation(s) provided!");
    }

    @Transactional
    public AppUser updateUserRoles(String userEmail, String roleToBeUpdated, String operation, AppUser loggedInUser) {

        String[] operations = validateRolesOperations(operation);
        Optional<AppUser> userOpt = findUserAndThrowExceptionIfDoesExist(userEmail);

        AppUser user = userOpt.get();
        List<SecurityEvent> securityEvents = new ArrayList<>();

        for(String op : operations) {

            if(op.equalsIgnoreCase(GRANT.getAction())){
                grantNewRoleToUser(user.getRoles(),  "ROLE_"+roleToBeUpdated, user.getUsername(),
                        loggedInUser.getUsername(),securityEvents);
            } else {
                user.setRoles(removeUserRole(user.getRoles(), "ROLE_"+roleToBeUpdated, user.getUsername(),
                        loggedInUser, securityEvents));
            }

        }

        AppUser savedUser = userRepository.save(user);
        securityEventsService.recordSecurityEvents(securityEvents);
        return savedUser;
    }

    private void grantNewRoleToUser(List<Role> existentUserRoles, String newRole, String userName, String loggedInUser,
                                    List<SecurityEvent> securityEvents){

        log.debug("Granting new role {} to user {}", newRole, userName);
        log.debug("Existent user roles: {}", existentUserRoles.stream().map(Role::getName).collect(Collectors.toList()));

        blockGrantingBusinessRoleToAdministrator(existentUserRoles, newRole, userName);
        blockGrantingDuplicatedRoles(existentUserRoles, newRole, userName);

        Role retrievedRole = checkIfProvidedRoleExistAndReturnIt(newRole, userName);
        existentUserRoles.add(retrievedRole);

        String roleNameToAudit = newRole.split("_")[1];

        securityEvents.add(grantRoleEvent(loggedInUser,roleNameToAudit, userName));
    }

    private void blockGrantingBusinessRoleToAdministrator(List<Role> existentUserRoles, String newRole, String userName){

        boolean hasNonAdministrativeRoles = !existentUserRoles.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(ADMINISTRATOR.getName()));

        boolean newRoleIsAdministrative = newRole.equalsIgnoreCase(ADMINISTRATOR.getName());

        if((newRoleIsAdministrative && hasNonAdministrativeRoles) ||
                (!newRoleIsAdministrative && !hasNonAdministrativeRoles)){
            log.error("Not possible to combine administrative and business roles into user {}", userName);
            throw new InvalidRoleException("The user cannot combine administrative and business roles!");
        }
    }

    private void blockGrantingDuplicatedRoles(List<Role> existentUserRoles, String newRole, String userName){
        if(existentUserRoles.stream().anyMatch(r -> r.getName().equalsIgnoreCase(newRole))){
            log.error("Not possible add duplicated role with name {} to user {}", newRole, userName);
            throw new InvalidRoleException("Not possible to add duplicated role!");
        }
    }

    private Role checkIfProvidedRoleExistAndReturnIt(String newRole, String userName){
        Optional<Role> retrievedRole = roleRepository.findByNameIgnoreCase(newRole);
        if(!retrievedRole.isPresent()){
            log.error("Not possible add role with name {} to user {} as it does not exist", newRole,userName);
            throw new RoleNotFoundException();
        }
        return retrievedRole.get();
    }

    private void blockAdministratorRoleRemoval(List<Role> existentUserRoles,  String roleTobeRemoved, String username){
        if(roleTobeRemoved.equalsIgnoreCase(ADMINISTRATOR.getName()) && existentUserRoles.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(ADMINISTRATOR.getName()))){
            log.error("Not possible to remove administrator role from user {}", username);
            throw new InvalidRoleException("Can't remove ADMINISTRATOR role!");
        }
    }

    private void blockRemovalOfUnassignedRole(List<Role> existentUserRoles,  String roleTobeRemoved, String username){
        if(!existentUserRoles.stream().anyMatch(r -> r.getName().equalsIgnoreCase(roleTobeRemoved))){
            log.error("Not possible to remove role with name {} from user {}", roleTobeRemoved, username);
            throw new InvalidRoleException("The user does not have a role!");
        }
    }

    private void blockUniqueRoleRemoval(List<Role> existentUserRoles,  String roleTobeRemoved, String username){
        if(existentUserRoles.size() == 1 &&
                existentUserRoles.stream().anyMatch(r -> r.getName().equalsIgnoreCase(roleTobeRemoved))){
            log.error("Not possible to delete the unique user role: {}, {}", roleTobeRemoved, username);
            throw new InvalidRoleException("The user must have at least one role!");
        }
    }

    private List<Role> removeUserRole(List<Role> existentUserRoles, String roleTobeRemoved, String username, AppUser loggedInUser, List<SecurityEvent> securityEvents){

        blockAdministratorRoleRemoval(existentUserRoles, roleTobeRemoved, username);
        blockRemovalOfUnassignedRole(existentUserRoles, roleTobeRemoved, username);
        blockUniqueRoleRemoval(existentUserRoles, roleTobeRemoved, username);

        List<Role> updatedUserRoles = existentUserRoles.stream()
                .filter(r -> !r.getName().equalsIgnoreCase(roleTobeRemoved))
                .collect(Collectors.toList());

        String roleNameToAudit = roleTobeRemoved.split("_")[1];

        securityEvents.add(removeRoleEvent(loggedInUser.getUsername(),
                roleNameToAudit, username));

        return updatedUserRoles;
    }

    public List<AppUser> findAllUsers(){
        return userRepository.findAll();
    }

    @Transactional
    public void lockAndUnlockUser(String action, String username){

        AccessActionEnum actionEnum = validateUserLockAction(action);

        Optional<AppUser> userOpt = findUserAndThrowExceptionIfDoesExist(username);

        AppUser user = userOpt.get();
        blockLockingAdministrator(user, actionEnum);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication.getName();

        if(actionEnum.equals(AccessActionEnum.LOCK)){
            user.setAccountNonLocked(false);
            securityEventsService.recordLockUserEvent(principal, username);
        }

        if(actionEnum.equals(AccessActionEnum.UNLOCK)){
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            securityEventsService.recordUnlockUserEvent(principal, username);
        }

        AppUser savedUser = userRepository.save(user);
        log.info("Updated user {} isAccountNonLocked state to {}", username,  savedUser.isAccountNonLocked());
    }

    private AccessActionEnum validateUserLockAction(String action){
        if(action.equalsIgnoreCase("lock")) {
            return AccessActionEnum.LOCK;
        } else if(action.equalsIgnoreCase("unlock")){
            return AccessActionEnum.UNLOCK;
        } else {
            throw new InvalidUserActionException("Invalid user action!");
        }
    }

    private void blockLockingAdministrator(AppUser retrievedUser, AccessActionEnum actionEnum){
        if(retrievedUser.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(ADMINISTRATOR.getName()))
                && actionEnum.equals(AccessActionEnum.LOCK)){
            log.error("Not possible {} an administrator user", actionEnum.getName());
            throw new InvalidUserActionException(String.format("Can't %s the ADMINISTRATOR!", actionEnum.getName()));
        }
    }
}

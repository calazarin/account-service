package account.controller;

import account.dto.AccessActionDto;
import account.dto.ChangePassDto;
import account.dto.ChangePassRespDto;
import account.dto.RoleActionDto;
import account.dto.StatusDto;
import account.dto.UserDto;
import account.entity.AppUser;
import account.entity.Role;
import account.enums.AccessActionEnum;
import account.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    public static final String AUHT_SIGNUP_URL = "/api/auth/signup";
    public static final String AUTH_CHANGE_PASS_URL = "/api/auth/changepass";
    public static final String ADMIN_USER_ROLE = "/api/admin/user/role";
    public static final String ADMIN_USER = "/api/admin/user/";
    public static final String ADMIN_USER_ACCESS = "/api/admin/user/access";

    public static final String ADMIN_USER_DELETE = "/api/admin/user";

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @Operation(summary = "Creates a new user")
    @PostMapping(path = AUHT_SIGNUP_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDto signUp(@Valid @RequestBody UserDto userReqDto){

         AppUser createdUser = userService.registerNewUser(userReqDto.getName(), userReqDto.getLastname(),
                userReqDto.getEmail(), userReqDto.getPassword());

        return new UserDto(createdUser.getId(),
                createdUser.getName(),
                createdUser.getLastName(),
                createdUser.getUsername().toLowerCase(),
                createdUser.getRoles()
                        .stream()
                        .map(Role::getName).
                        sorted()
                        .collect(Collectors.toList()));
    }

    @Operation(summary = "Changes user's password")
    @PostMapping(path = AUTH_CHANGE_PASS_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChangePassRespDto changePass(@Valid @RequestBody ChangePassDto changePassDto,
                                        @AuthenticationPrincipal AppUser loggedInUser){

        String userEmail = loggedInUser.getUsername();
        userService.updateUserPassword(userEmail, changePassDto.getNewPassword());
        final String status = "The password has been updated successfully";
        return new ChangePassRespDto(userEmail.toLowerCase(), status);
    }

    @Operation(summary = "Adds/removes an user role")
    @PutMapping(path = ADMIN_USER_ROLE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDto setUserRole(@RequestBody RoleActionDto actionDto,
                               @AuthenticationPrincipal AppUser loggedInUser){
        AppUser user = userService.updateUserRoles(actionDto.getUser(), actionDto.getRole(), actionDto.getOperation(),
                loggedInUser);
        return new UserDto(user.getId(),
                user.getName(),
                user.getLastName(),
                user.getUsername().toLowerCase(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .sorted()
                        .collect(Collectors.toList()));
    }

    @Operation(summary = "Removes an user from system")
    @DeleteMapping(value = {"/api/admin/user/{userEmail}", "/api/admin/user/"})
    public StatusDto deleteUser(@AuthenticationPrincipal AppUser loggedInUser,
                                @PathVariable(required = false) Optional<String> userEmail){

        String loggedInUsername = loggedInUser.getUsername();
        if(userEmail.isPresent()){
            log.info("Deleting user passed by path variable: {}", userEmail.get());
            this.userService.deleteUser(loggedInUsername, userEmail.get());
            return new StatusDto(userEmail.get(), "Deleted successfully!");
        }
        return new StatusDto(loggedInUsername, "Deleted successfully!");
    }

    @Operation(summary = "Retrieves all users")
    @GetMapping(path = ADMIN_USER, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<UserDto> retrieveAllUsers(){
        return userService.findAllUsers().stream()
                .sorted(Comparator.comparingLong(AppUser::getId))
                .map(user ->
                new UserDto(user.getId(),
                        user.getName(),
                        user.getLastName(),
                        user.getUsername().toLowerCase(),
                        user.getRoles().stream()
                                .map(Role::getName)
                                .sorted()
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Locks/unlocks an user")
    @PutMapping(path = ADMIN_USER_ACCESS, consumes = MediaType.APPLICATION_JSON_VALUE)
    public StatusDto lockAndUnlockUser(@Valid @RequestBody AccessActionDto actionDto){

        userService.lockAndUnlockUser(actionDto.getOperation(), actionDto.getUser());

        String action = actionDto.getOperation().equalsIgnoreCase(AccessActionEnum.LOCK.name()) ?
                AccessActionEnum.LOCK.getStatus() : AccessActionEnum.UNLOCK.getStatus();

        return new StatusDto(String.format("User %s %s!", actionDto.getUser().toLowerCase(), action));
    }
}


package account.enums;

import lombok.Getter;

@Getter
public enum SecureEventActionEnum {

    CREATE_USER("CREATE_USER", "A user has been successfully registered"),
    CHANGE_PASSWORD("CHANGE_PASSWORD","A user has changed the password successfully"),
    ACCESS_DENIED("ACCESS_DENIED","A user is trying to access a resource without access rights"),
    LOGIN_FAILED("LOGIN_FAILED","Failed authentication"),
    GRANT_ROLE("GRANT_ROLE","A role is granted to a user"),
    REMOVE_ROLE("REMOVE_ROLE","A role has been revoked"),
    LOCK_USER("LOCK_USER","The Administrator has locked the user"),
    UNLOCK_USER("UNLOCK_USER","The Administrator has unlocked a user"),
    DELETE_USER("DELETE_USER","The Administrator has deleted a user"),
    BRUTE_FORCE("BRUTE_FORCE","A user has been blocked on suspicion of a brute force attack");

    private String name;
    private String description;

    private SecureEventActionEnum(String name, String description){
        this.name = name;
        this.description = description;
    }
}

package account.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    ADMINISTRATOR("ROLE_ADMINISTRATOR", "ADMINISTRATOR"),
    USER("ROLE_USER", "USER"),
    ACCOUNTANT("ROLE_ACCOUNTANT", "ACCOUNTANT"),
    AUDITOR("ROLE_AUDITOR", "AUDITOR");

    private String name;

    private String shortName;

    UserRoleEnum(String name, String shortName){
        this.name = name;
        this.shortName = shortName;
    }

}

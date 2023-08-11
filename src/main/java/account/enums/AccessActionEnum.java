package account.enums;

import lombok.Getter;

@Getter
public enum AccessActionEnum {
    LOCK("lock", "locked"),
    UNLOCK("unlock", "unlocked");

    private String name;
    private String status;

    private AccessActionEnum(String name, String status){
        this.name = name;
        this.status = status;
    }
}

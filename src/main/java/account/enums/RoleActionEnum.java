package account.enums;

public enum RoleActionEnum {

    GRANT("GRANT"),
    REMOVE("REMOVE");

    private String action;

    RoleActionEnum(String action){
        this.action = action;
    }

    public String getAction(){
        return this.action;
    }
}

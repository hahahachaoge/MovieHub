package com.movie.enums;

public enum UserRoleEnum {
    VIP("VIP", "VIP会员"),
    NORMAL("NORMAL", "普通用户"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String desc;

    UserRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

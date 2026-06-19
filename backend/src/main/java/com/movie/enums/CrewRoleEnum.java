package com.movie.enums;

public enum CrewRoleEnum {
    ACTOR("ACTOR", "演员"),
    DIRECTOR("DIRECTOR", "导演");

    private final String code;
    private final String desc;

    CrewRoleEnum(String code, String desc) {
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

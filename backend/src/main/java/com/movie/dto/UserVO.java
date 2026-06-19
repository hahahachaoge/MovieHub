package com.movie.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private String vipExpireTime;
    private java.math.BigDecimal balance;
    private String createTime;
}

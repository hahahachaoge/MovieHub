package com.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("crew")
public class Crew {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatar;

    private String bio;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("role_type")
    private String roleType;

    // 非数据库字段：是否同时是导演和演员
    @TableField(exist = false)
    private String displayRole;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

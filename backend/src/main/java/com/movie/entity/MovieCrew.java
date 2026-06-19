package com.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("movie_crew")
public class MovieCrew {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("movie_id")
    private Long movieId;

    @TableField("crew_id")
    private Long crewId;

    @TableField("character_name")
    private String characterName;

    private String position;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 非数据库字段：主创详细信息
    @TableField(exist = false)
    private Crew crew;
}

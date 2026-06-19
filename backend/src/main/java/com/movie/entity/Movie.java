package com.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("movie")
public class Movie {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("original_title")
    private String originalTitle;

    @TableField("cover_url")
    private String coverUrl;

    private String description;

    @TableField("release_date")
    private LocalDate releaseDate;

    private Integer duration;

    private String region;

    private String language;

    private BigDecimal rating;

    @TableField("rating_count")
    private Integer ratingCount;

    @TableField("play_url")
    private String playUrl;

    @TableField("is_vip")
    private Boolean isVip;

    @TableField("play_count")
    private Long playCount;

    @TableField("is_hot")
    private Boolean isHot;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段：分类列表
    @TableField(exist = false)
    private List<Category> categories;

    // 非数据库字段：主创列表
    @TableField(exist = false)
    private List<MovieCrew> crews;
}

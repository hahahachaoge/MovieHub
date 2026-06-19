package com.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("movie_type")
public class MovieType {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("movie_id")
    private Long movieId;

    @TableField("category_id")
    private Long categoryId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

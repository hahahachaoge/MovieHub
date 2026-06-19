package com.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("play_record")
public class PlayRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("movie_id")
    private Long movieId;

    @TableField("watch_duration")
    private Integer watchDuration;

    private BigDecimal progress;

    @TableField("watch_date")
    private LocalDate watchDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

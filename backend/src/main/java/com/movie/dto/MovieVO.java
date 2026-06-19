package com.movie.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MovieVO {
    private Long id;
    private String title;
    private String originalTitle;
    private String coverUrl;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private String region;
    private String language;
    private BigDecimal rating;
    private Integer ratingCount;
    private String playUrl;
    private Boolean isVip;
    private Long playCount;
    private Boolean isHot;
    private Integer status;

    // 关联数据
    private List<String> categoryNames;
    private List<CrewVO> directors;
    private List<CrewVO> actors;
}

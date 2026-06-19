package com.movie.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RankingVO {
    private int rank;
    private Long movieId;
    private String title;
    private String coverUrl;
    private BigDecimal rating;
    private Long playCount;
    private String region;
    private String categoryNames;
}

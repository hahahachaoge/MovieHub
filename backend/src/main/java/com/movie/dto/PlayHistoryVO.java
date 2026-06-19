package com.movie.dto;

import lombok.Data;

@Data
public class PlayHistoryVO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String coverUrl;
    private String watchDate;
    private Integer watchDuration;
    private String progress;
}

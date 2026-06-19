package com.movie.dto;

import lombok.Data;

@Data
public class CrewVO {
    private Long id;
    private String name;
    private String avatar;
    private String roleType;
    private String characterName;
    private String position;
}

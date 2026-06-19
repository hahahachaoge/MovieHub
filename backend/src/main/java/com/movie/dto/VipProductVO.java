package com.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class VipProductVO {
    private String id;
    private String name;
    private BigDecimal price;
    private int months;
    private String badge;
    private String[] benefits;
    private List<String> disabledBenefits;
}
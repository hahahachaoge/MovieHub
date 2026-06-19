package com.movie.controller;

import com.movie.common.Result;
import com.movie.dto.RankingVO;
import com.movie.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping("/weekly")
    public Result<List<RankingVO>> getWeeklyRanking() {
        return Result.success(rankingService.getWeeklyRanking());
    }

    @GetMapping("/monthly")
    public Result<List<RankingVO>> getMonthlyRanking() {
        return Result.success(rankingService.getMonthlyRanking());
    }

    @GetMapping("/all")
    public Result<List<RankingVO>> getAllTimeRanking() {
        return Result.success(rankingService.getAllTimeRanking());
    }

    @GetMapping("/top-rated")
    public Result<List<RankingVO>> getTopRated() {
        return Result.success(rankingService.getTopRated());
    }
}

package com.movie.controller;

import com.movie.common.Result;
import com.movie.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 管理员Dashboard数据统计
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(reportService.getDashboardStats());
    }

    /**
     * 导出Excel排行榜
     */
    @GetMapping("/export/{type}")
    public void exportRanking(@PathVariable String type, HttpServletResponse response) {
        reportService.exportRanking(type, response);
    }

    /**
     * 评分分布统计
     */
    @GetMapping("/rating-distribution")
    public Result<Map<String, Object>> getRatingDistribution() {
        return Result.success(reportService.getRatingDistribution());
    }

    /**
     * 地区分布统计
     */
    @GetMapping("/region-distribution")
    public Result<Map<String, Object>> getRegionDistribution() {
        return Result.success(reportService.getRegionDistribution());
    }

    /**
     * 月度播放量趋势
     */
    @GetMapping("/monthly-play-trend")
    public Result<Map<String, Object>> getMonthlyPlayTrend(@RequestParam(defaultValue = "2026") int year) {
        return Result.success(reportService.getMonthlyPlayTrend(year));
    }

    /**
     * 每日播放量趋势（最近7天）
     */
    @GetMapping("/daily-play-trend")
    public Result<Map<String, Object>> getDailyPlayTrend() {
        return Result.success(reportService.getDailyPlayTrend());
    }
}

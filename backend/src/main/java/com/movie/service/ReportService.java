package com.movie.service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface ReportService {

    /**
     * 管理员Dashboard数据统计
     */
    Map<String, Object> getDashboardStats();

    /**
     * 导出电影排行Excel报表
     */
    void exportRanking(String type, HttpServletResponse response);

    /**
     * 评分分布统计
     */
    Map<String, Object> getRatingDistribution();

    /**
     * 地区分布统计
     */
    Map<String, Object> getRegionDistribution();

    /**
     * 月度播放量趋势
     */
    Map<String, Object> getMonthlyPlayTrend(int year);

    /**
     * 每日播放量趋势（最近7天）
     */
    Map<String, Object> getDailyPlayTrend();
}

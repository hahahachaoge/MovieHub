package com.movie.service;

import com.movie.dto.RankingVO;
import com.movie.dto.MovieVO;

import java.util.List;

public interface RankingService {

    /**
     * 本周排行（按播放量）
     */
    List<RankingVO> getWeeklyRanking();

    /**
     * 本月排行（按播放量）
     */
    List<RankingVO> getMonthlyRanking();

    /**
     * 全部排行（按播放量）
     */
    List<RankingVO> getAllTimeRanking();

    /**
     * 好评排行（按评分）
     */
    List<RankingVO> getTopRated();

    /**
     * 记录播放（增加播放计数）
     */
    void recordPlay(Long movieId);
}

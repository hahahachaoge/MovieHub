package com.movie.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.movie.dto.RankingVO;
import com.movie.entity.Movie;
import com.movie.entity.PlayRecord;
import com.movie.mapper.MovieMapper;
import com.movie.mapper.PlayRecordMapper;
import com.movie.service.MovieService;
import com.movie.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    private PlayRecordMapper playRecordMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private MovieService movieService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String WEEKLY_KEY = "movie:ranking:weekly";
    private static final String MONTHLY_KEY = "movie:ranking:monthly";
    private static final String ALL_KEY = "movie:ranking:all";
    private static final String RATING_KEY = "movie:ranking:rating";

    @Override
    public List<RankingVO> getWeeklyRanking() {
        @SuppressWarnings("unchecked")
        List<RankingVO> cached = (List<RankingVO>) redisTemplate.opsForValue().get(WEEKLY_KEY);
        if (cached != null) return cached;

        List<RankingVO> list = computeRankingFromDB(
                LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                LocalDate.now());
        if (!list.isEmpty()) {
            redisTemplate.opsForValue().set(WEEKLY_KEY, list, 10, TimeUnit.MINUTES);
        }
        return list;
    }

    @Override
    public List<RankingVO> getMonthlyRanking() {
        @SuppressWarnings("unchecked")
        List<RankingVO> cached = (List<RankingVO>) redisTemplate.opsForValue().get(MONTHLY_KEY);
        if (cached != null) return cached;

        List<RankingVO> list = computeRankingFromDB(
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now());
        if (!list.isEmpty()) {
            redisTemplate.opsForValue().set(MONTHLY_KEY, list, 10, TimeUnit.MINUTES);
        }
        return list;
    }

    @Override
    public List<RankingVO> getAllTimeRanking() {
        @SuppressWarnings("unchecked")
        List<RankingVO> cached = (List<RankingVO>) redisTemplate.opsForValue().get(ALL_KEY);
        if (cached != null) return cached;

        List<Movie> movies = movieMapper.selectList(
                new QueryWrapper<Movie>()
                        .eq("status", 1)
                        .orderByDesc("play_count")
                        .last("LIMIT 20")
        );

        List<RankingVO> list = buildRanking(movies, "playCount");
        redisTemplate.opsForValue().set(ALL_KEY, list, 10, TimeUnit.MINUTES);
        return list;
    }

    @Override
    public List<RankingVO> getTopRated() {
        @SuppressWarnings("unchecked")
        List<RankingVO> cached = (List<RankingVO>) redisTemplate.opsForValue().get(RATING_KEY);
        if (cached != null) return cached;

        List<Movie> movies = movieMapper.selectList(
                new QueryWrapper<Movie>()
                        .eq("status", 1)
                        .gt("rating_count", 10)
                        .orderByDesc("rating")
                        .last("LIMIT 20")
        );

        List<RankingVO> list = buildRanking(movies, "rating");
        redisTemplate.opsForValue().set(RATING_KEY, list, 1, TimeUnit.HOURS);
        return list;
    }

    @Override
    public void recordPlay(Long movieId) {
        // 有新的播放记录时，清除周/月排行缓存，下次请求自动从DB拉最新数据
        redisTemplate.delete(WEEKLY_KEY);
        redisTemplate.delete(MONTHLY_KEY);
    }

    /**
     * 定时任务：每10分钟刷新排行榜缓存
     */
    @Scheduled(fixedRate = 600000)
    public void refreshRankings() {
        // 清除缓存，下次访问时重新计算
        redisTemplate.delete(WEEKLY_KEY);
        redisTemplate.delete(MONTHLY_KEY);
        redisTemplate.delete(ALL_KEY);
        redisTemplate.delete(RATING_KEY);
    }

    private List<RankingVO> computeRankingFromDB(LocalDate from, LocalDate to) {
        // 查询日期范围内的播放记录，按电影分组统计
        List<Map<String, Object>> records = playRecordMapper.selectMaps(
                new QueryWrapper<PlayRecord>()
                        .select("movie_id, COUNT(*) as play_count")
                        .ge("watch_date", from)
                        .le("watch_date", to)
                        .groupBy("movie_id")
                        .orderByDesc("play_count")
                        .last("LIMIT 20")
        );

        if (records.isEmpty()) return new ArrayList<>();

        List<RankingVO> list = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> record : records) {
            Long movieId = ((Number) record.get("movie_id")).longValue();
            Long count = ((Number) record.get("play_count")).longValue();

            Movie movie = movieMapper.selectById(movieId);
            if (movie == null || movie.getStatus() == 0) continue;

            RankingVO vo = new RankingVO();
            vo.setRank(rank++);
            vo.setMovieId(movie.getId());
            vo.setTitle(movie.getTitle());
            vo.setCoverUrl(movie.getCoverUrl());
            vo.setRating(movie.getRating());
            vo.setPlayCount(count);
            vo.setRegion(movie.getRegion());
            list.add(vo);
        }
        return list;
    }

    private List<RankingVO> buildRanking(List<Movie> movies, String sortBy) {
        List<RankingVO> list = new ArrayList<>();
        int rank = 1;
        for (Movie movie : movies) {
            RankingVO vo = new RankingVO();
            vo.setRank(rank++);
            vo.setMovieId(movie.getId());
            vo.setTitle(movie.getTitle());
            vo.setCoverUrl(movie.getCoverUrl());
            vo.setRating(movie.getRating());
            vo.setPlayCount(movie.getPlayCount());
            vo.setRegion(movie.getRegion());
            list.add(vo);
        }
        return list;
    }
}

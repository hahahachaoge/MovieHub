package com.movie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.BusinessException;
import com.movie.dto.PlayHistoryVO;
import com.movie.entity.Movie;
import com.movie.entity.PlayRecord;
import com.movie.entity.User;
import com.movie.mapper.MovieMapper;
import com.movie.mapper.PlayRecordMapper;
import com.movie.mapper.UserMapper;
import com.movie.service.PlayService;
import com.movie.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlayServiceImpl implements PlayService {

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PlayRecordMapper playRecordMapper;

    @Autowired
    private RankingService rankingService;

    @Override
    public String playMovie(Long userId, Long movieId) {
        // 查询电影
        Movie movie = movieMapper.selectById(movieId);
        if (movie == null) {
            throw new BusinessException(404, "电影不存在");
        }
        if (movie.getStatus() == 0) {
            throw new BusinessException(400, "该电影已下架");
        }

        // VIP权限校验
        if (movie.getIsVip() != null && movie.getIsVip()) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(401, "用户不存在");
            }

            boolean isVip = "VIP".equals(user.getRole()) || "ADMIN".equals(user.getRole());
            if (!isVip && (user.getVipExpireTime() == null || user.getVipExpireTime().isBefore(LocalDateTime.now()))) {
                throw new BusinessException(403, "该影片为VIP专享，请充值后观看");
            }
        }

        // 记录播放（每次播放都新增记录，不限次数）
        LocalDate today = LocalDate.now();
        PlayRecord record = new PlayRecord();
        record.setUserId(userId);
        record.setMovieId(movieId);
        record.setWatchDate(today);
        record.setWatchDuration(0);
        record.setProgress(BigDecimal.ZERO);
        playRecordMapper.insert(record);

        // 更新播放计数
        movie.setPlayCount(movie.getPlayCount() == null ? 1 : movie.getPlayCount() + 1);
        movieMapper.updateById(movie);

        // 记录排行计数
        rankingService.recordPlay(movieId);

        return movie.getPlayUrl();
    }

    @Override
    public Page<PlayHistoryVO> getPlayHistory(Long userId, int page, int size) {
        Page<PlayRecord> recordPage = playRecordMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<PlayRecord>()
                        .eq("user_id", userId)
                        .orderByDesc("watch_date", "create_time")
        );

        Page<PlayHistoryVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        List<PlayHistoryVO> voList = new ArrayList<>();

        for (PlayRecord record : recordPage.getRecords()) {
            PlayHistoryVO vo = new PlayHistoryVO();
            vo.setId(record.getId());
            vo.setMovieId(record.getMovieId());
            vo.setWatchDate(record.getWatchDate().toString());
            vo.setWatchDuration(record.getWatchDuration());
            vo.setProgress(record.getProgress().toString() + "%");

            Movie movie = movieMapper.selectById(record.getMovieId());
            if (movie != null) {
                vo.setMovieTitle(movie.getTitle());
                vo.setCoverUrl(movie.getCoverUrl());
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }
}

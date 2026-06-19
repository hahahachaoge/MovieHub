package com.movie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.dto.MovieVO;
import com.movie.dto.PlayHistoryVO;

public interface PlayService {

    /**
     * 播放电影（校验权限）
     * @param userId 用户ID
     * @param movieId 电影ID
     * @return 播放URL
     */
    String playMovie(Long userId, Long movieId);

    /**
     * 获取用户播放历史
     */
    Page<PlayHistoryVO> getPlayHistory(Long userId, int page, int size);
}

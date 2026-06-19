package com.movie.controller;

import com.movie.common.Result;
import com.movie.dto.PlayHistoryVO;
import com.movie.dto.MovieVO;
import com.movie.service.MovieService;
import com.movie.service.PlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/movie")
public class PlayController {

    @Autowired
    private PlayService playService;

    @Autowired
    private MovieService movieService;

    /**
     * 播放电影（需认证）
     */
    @PostMapping("/play/{movieId}")
    public Result<Map<String, Object>> playMovie(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long movieId) {
        String playUrl = playService.playMovie(userId, movieId);
        MovieVO movie = movieService.getMovieDetail(movieId);

        Map<String, Object> data = new HashMap<>();
        data.put("playUrl", playUrl);
        data.put("movie", movie);
        return Result.success(data);
    }

    /**
     * 获取播放历史
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getHistory(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = playService.getPlayHistory(userId, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return Result.success(data);
    }
}

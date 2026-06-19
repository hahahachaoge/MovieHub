package com.movie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.Result;
import com.movie.dto.MovieVO;
import com.movie.entity.Category;
import com.movie.entity.Movie;
import com.movie.entity.MovieType;
import com.movie.mapper.MovieMapper;
import com.movie.mapper.MovieTypeMapper;
import com.movie.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movie/public")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private MovieTypeMapper movieTypeMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/all")
    public Result<Map<String, Object>> getAllMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        QueryWrapper<Movie> qw = new QueryWrapper<Movie>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like("title", keyword);
        }
        qw.orderByDesc("play_count");
        Page<Movie> moviePage = movieMapper.selectPage(new Page<>(page, size), qw);
        Page<MovieVO> voPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        voPage.setRecords(moviePage.getRecords().stream().map(m -> movieService.convertToVO(m)).collect(Collectors.toList()));
        Map<String, Object> data = new HashMap<>();
        data.put("records", voPage.getRecords());
        data.put("total", voPage.getTotal());
        data.put("page", voPage.getCurrent());
        data.put("size", voPage.getSize());
        return Result.success(data);
    }

    @GetMapping("/carousel")
    public Result<List<MovieVO>> getCarouselMovies() {
        List<MovieVO> movies = new ArrayList<>();
        long[] ids = {29, 62, 110, 148, 212};
        String[] carouselFiles = {
            "/tmdb_all_movie/carousel/你的名字。轮播.jpg",
            "/tmdb_all_movie/carousel/夏洛特烦恼轮播.png",
            "/tmdb_all_movie/carousel/摆渡人轮播.jpg",
            "/tmdb_all_movie/carousel/流浪地球2轮播.jpg",
            "/tmdb_all_movie/carousel/蝙蝠侠黑暗骑士崛起轮播.jpg"
        };
        for (int i = 0; i < ids.length; i++) {
            MovieVO vo = movieService.getMovieDetail(ids[i]);
            if (vo != null) {
                vo.setCoverUrl(carouselFiles[i]);
                movies.add(vo);
            }
        }
        return Result.success(movies);
    }

    @GetMapping("/hot")
    public Result<List<MovieVO>> getHotMovies(@RequestParam(defaultValue = "10") int limit) {
        List<MovieVO> movies = movieService.getHotMovies(limit);
        return Result.success(movies);
    }

    @GetMapping("/category/{categoryId}")
    public Result<Map<String, Object>> getMoviesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        var pageResult = movieService.getMoviesByCategory(categoryId, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return Result.success(data);
    }

    @GetMapping("/region/{region}")
    public Result<Map<String, Object>> getMoviesByRegion(
            @PathVariable String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        var pageResult = movieService.getMoviesByRegion(region, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return Result.success(data);
    }

    @GetMapping("/detail/{id}")
    public Result<MovieVO> getMovieDetail(@PathVariable Long id) {
        MovieVO movie = movieService.getMovieDetail(id);
        if (movie == null) return Result.notFound("电影不存在");
        return Result.success(movie);
    }

    @GetMapping("/categories")
    public Result<List<Category>> getAllCategories() {
        return Result.success(movieService.getAllCategories());
    }

    @GetMapping("/regions")
    public Result<List<String>> getAllRegions() {
        return Result.success(movieService.getAllRegions());
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> searchMovies(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        var pageResult = movieService.searchMovies(keyword, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return Result.success(data);
    }

    @GetMapping("/filter")
    public Result<Map<String, Object>> filterMovies(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<MovieVO> pageResult;
        if (keyword != null && !keyword.isEmpty()) {
            pageResult = movieService.searchMovies(keyword, page, size);
        } else {
            pageResult = movieService.getMoviesByCategoryAndRegion(categoryId, region, page, size);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return Result.success(data);
    }

    @GetMapping("/latest")
    public Result<List<MovieVO>> getLatestMovies(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(movieService.getLatestMovies(limit));
    }

    @GetMapping("/related/{id}")
    public Result<List<MovieVO>> getRelatedMovies(@PathVariable Long id) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null) return Result.success(new ArrayList<>());
        Set<Long> ids = new LinkedHashSet<>();
        List<Long> catIds = movieMapper.selectCategoryIdsByMovieId(id);
        for (Long cid : catIds) {
            movieTypeMapper.selectList(new QueryWrapper<MovieType>().eq("category_id", cid))
                .stream().map(MovieType::getMovieId).filter(mid -> !mid.equals(id))
                .forEach(mid -> { if (ids.size() < 6) ids.add(mid); });
        }
        if (ids.size() < 6 && movie.getRegion() != null) {
            movieMapper.selectList(new QueryWrapper<Movie>().eq("region", movie.getRegion()).eq("status", 1).orderByDesc("play_count").last("LIMIT 10"))
                .stream().map(Movie::getId).filter(mid -> !mid.equals(id))
                .forEach(mid -> { if (ids.size() < 6) ids.add(mid); });
        }
        if (ids.size() < 6) {
            movieMapper.selectList(new QueryWrapper<Movie>().eq("status", 1).orderByDesc("play_count").last("LIMIT 10"))
                .stream().map(Movie::getId).filter(mid -> !mid.equals(id))
                .forEach(mid -> { if (ids.size() < 6) ids.add(mid); });
        }
        List<MovieVO> result = ids.stream().map(movieService::getMovieDetail).filter(Objects::nonNull).collect(Collectors.toList());
        return Result.success(result);
    }

    @PutMapping("/update-status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        Movie movie = movieMapper.selectById(id);
        if (movie != null) {
            movie.setStatus(status);
            movieMapper.updateById(movie);
            // 清除缓存，让首页重新加载
            redisTemplate.delete("movie:hot");
        }
        return Result.success();
    }

    @GetMapping("/video/{id}")
    public void streamVideo(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        try {
            Movie movie = movieMapper.selectById(id);
            if (movie == null) { response.setStatus(404); return; }
            File trailerDir = new File("./tmdb_all_movie/" + movie.getTitle() + "/trailer/");
            if (!trailerDir.exists()) trailerDir = new File("../tmdb_all_movie/" + movie.getTitle() + "/trailer/");
            if (!trailerDir.exists()) { response.setStatus(404); return; }
            File[] files = trailerDir.listFiles((dir, name) -> name.endsWith(".mp4"));
            if (files == null || files.length == 0) { response.setStatus(404); return; }
            File videoFile = files[0];
            long fileLength = videoFile.length();
            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileLength - 1;
                long cl = end - start + 1;
                response.setStatus(206);
                response.setContentType("video/mp4");
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.setHeader("Accept-Ranges", "bytes");
                response.setContentLengthLong(cl);
                try (RandomAccessFile raf = new RandomAccessFile(videoFile, "r"); OutputStream os = response.getOutputStream()) {
                    raf.seek(start); byte[] buf = new byte[8192]; long rem = cl;
                    while (rem > 0) { int r = raf.read(buf, 0, (int) Math.min(buf.length, rem)); if (r == -1) break; os.write(buf, 0, r); rem -= r; }
                    os.flush();
                }
            } else {
                response.setContentType("video/mp4");
                response.setHeader("Accept-Ranges", "bytes");
                response.setContentLengthLong(fileLength);
                try (FileInputStream fis = new FileInputStream(videoFile); OutputStream os = response.getOutputStream()) {
                    byte[] buf = new byte[8192]; int r;
                    while ((r = fis.read(buf)) != -1) os.write(buf, 0, r);
                    os.flush();
                }
            }
        } catch (Exception e) { response.setStatus(500); }
    }
}

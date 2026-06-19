package com.movie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.movie.common.Result;
import com.movie.dto.MovieVO;
import com.movie.entity.Category;
import com.movie.entity.Movie;
import com.movie.entity.MovieType;
import com.movie.entity.PlayRecord;
import com.movie.mapper.CategoryMapper;
import com.movie.mapper.MovieMapper;
import com.movie.mapper.MovieTypeMapper;
import com.movie.mapper.PlayRecordMapper;
import com.movie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private MovieTypeMapper movieTypeMapper;

    @Autowired
    private PlayRecordMapper playRecordMapper;

    @GetMapping("/search")
    public Result<Map<String, Object>> searchWithAI(@RequestParam String q) {
        String query = q.trim();

        // ===== 尝试提取电影名 =====
        String movieName = extractMovieNameFromSentence(query);
        if (movieName != null) {
            MovieVO movie = findMovieByTitle(movieName);
            if (movie != null) {
                String note = "您查询的电影《" + movie.getTitle() + "》";
                note += "，导演：" + (movie.getDirectors() != null && !movie.getDirectors().isEmpty()
                        ? movie.getDirectors().stream().map(d -> d.getName()).collect(Collectors.joining("、")) : "未知");
                note += "，评分：" + movie.getRating();
                if (movie.getDescription() != null && !movie.getDescription().isEmpty()) {
                    note += "，" + movie.getDescription();
                }
                List<MovieVO> result = new ArrayList<>();
                result.add(movie);
                Map<String, Object> data = new HashMap<>();
                data.put("records", result);
                data.put("total", 1);
                data.put("aiNote", note);
                return Result.success(data);
            }
        }

        // ===== 推荐类查询 =====
        List<Long> targetCategoryIds = new ArrayList<>();
        String[] categoryKeywords = {"动作", "喜剧", "爱情", "科幻", "动画", "悬疑", "搞笑", "浪漫", "奇幻", "战争", "冒险", "恐怖", "犯罪", "剧情"};
        for (String ck : categoryKeywords) {
            if (query.contains(ck)) {
                Category cat = categoryMapper.selectOne(new QueryWrapper<Category>().eq("name",
                    ck.equals("搞笑") ? "喜剧" :
                    ck.equals("浪漫") ? "爱情" :
                    ck.equals("奇幻") || ck.equals("冒险") ? "科幻" :
                    ck.equals("战争") || ck.equals("犯罪") ? "动作" :
                    ck.equals("恐怖") ? "悬疑" : ck
                ));
                if (cat != null && !targetCategoryIds.contains(cat.getId())) {
                    targetCategoryIds.add(cat.getId());
                }
            }
        }

        List<String> targetRegions = new ArrayList<>();
        if (query.contains("国产") || query.contains("中国") || query.contains("国内")) {
            targetRegions.add("中国大陆");
            targetRegions.add("中国香港");
            targetRegions.add("中国台湾");
        }
        if (query.contains("美国")) targetRegions.add("美国");
        if (query.contains("日本")) targetRegions.add("日本");
        if (query.contains("韩国") || query.contains("韩剧")) targetRegions.add("韩国");
        if (query.contains("英国")) targetRegions.add("英国");
        if (query.contains("法国")) targetRegions.add("法国");
        if (query.contains("国外") || query.contains("外国")) {
            List<Map<String, Object>> regionRecords = movieMapper.selectCountByRegion();
            for (Map<String, Object> rec : regionRecords) {
                String r = (String) rec.get("region");
                if (r != null && !r.contains("中国大陆") && !r.contains("中国香港") && !r.contains("中国台湾")) {
                    targetRegions.add(r);
                }
            }
        }

        Set<Long> matchedMovieIds = new LinkedHashSet<>();
        if (!targetCategoryIds.isEmpty()) {
            for (Long cid : targetCategoryIds) {
                List<Long> ids = movieTypeMapper.selectList(
                        new QueryWrapper<MovieType>().eq("category_id", cid)
                ).stream().map(MovieType::getMovieId).collect(Collectors.toList());
                matchedMovieIds.addAll(ids);
            }
        }

        if (!targetRegions.isEmpty()) {
            List<Movie> regionMovies = movieMapper.selectList(
                    new QueryWrapper<Movie>().in("region", targetRegions).eq("status", 1)
            );
            Set<Long> regionIds = regionMovies.stream().map(Movie::getId).collect(Collectors.toSet());
            if (!matchedMovieIds.isEmpty()) {
                matchedMovieIds.retainAll(regionIds);
            } else {
                matchedMovieIds = regionIds;
            }
        }

        List<MovieVO> finalMovies;
        boolean hasExplicitFilter = !targetCategoryIds.isEmpty() || !targetRegions.isEmpty();
        if (!matchedMovieIds.isEmpty()) {
            finalMovies = matchedMovieIds.stream()
                    .map(movieService::getMovieDetail)
                    .filter(Objects::nonNull)
                    .limit(12)
                    .collect(Collectors.toList());
        } else if (hasExplicitFilter) {
            // 有明确筛选条件但没匹配到，返回空（不fallback到关键词搜索）
            finalMovies = new ArrayList<>();
        } else {
            var pageResult = movieService.searchMovies(query, 1, 12);
            finalMovies = pageResult.getRecords();
        }

        if (finalMovies.size() < 6 && !hasExplicitFilter) {
            List<MovieVO> hot = movieService.getHotMovies(12);
            for (MovieVO h : hot) {
                if (finalMovies.stream().noneMatch(m -> m.getId().equals(h.getId()))) {
                    finalMovies.add(h);
                    if (finalMovies.size() >= 12) break;
                }
            }
        }

        String aiNote = generateSmartNote(query, finalMovies, targetCategoryIds, targetRegions);
        Map<String, Object> data = new HashMap<>();
        data.put("records", finalMovies);
        data.put("total", finalMovies.size());
        data.put("aiNote", aiNote);
        return Result.success(data);
    }

    private String extractMovieNameFromSentence(String text) {
        List<String> names = extractBookNames(text);
        if (!names.isEmpty()) return names.get(0);
        String cleaned = text;
        String[] prefixes = {"给我讲讲", "给我介绍", "我想看", "我要看", "看看", "讲一讲", "介绍一下", "说说", "介绍", "看", "讲"};
        for (String p : prefixes) {
            if (cleaned.startsWith(p)) {
                cleaned = cleaned.substring(p.length()).trim();
                break;
            }
        }
        String[] suffixes = {"这部电影", "这个电影", "这部片", "这部电影吧", "吧"};
        for (String su : suffixes) {
            if (cleaned.endsWith(su)) {
                cleaned = cleaned.substring(0, cleaned.length() - su.length()).trim();
                break;
            }
        }
        if (cleaned.contains("推荐") || cleaned.contains("有没有") || cleaned.contains("好看的")) {
            return null;
        }
        if (cleaned.length() <= 10 && cleaned.length() >= 1) {
            MovieVO found = findMovieByTitle(cleaned);
            if (found != null) return cleaned;
        }
        return null;
    }

    private List<String> extractBookNames(String text) {
        List<String> names = new ArrayList<>();
        int idx = 0;
        while ((idx = text.indexOf("《", idx)) != -1) {
            int end = text.indexOf("》", idx);
            if (end != -1) {
                names.add(text.substring(idx + 1, end));
                idx = end + 1;
            } else break;
        }
        return names;
    }

    private MovieVO findMovieByTitle(String title) {
        if (title == null || title.isEmpty()) return null;
        List<Movie> movies = movieMapper.selectList(
                new QueryWrapper<Movie>().eq("title", title).eq("status", 1).last("LIMIT 1")
        );
        if (!movies.isEmpty()) return movieService.convertToVO(movies.get(0));
        movies = movieMapper.selectList(
                new QueryWrapper<Movie>().like("title", title).eq("status", 1).orderByDesc("play_count").last("LIMIT 1")
        );
        if (!movies.isEmpty()) return movieService.convertToVO(movies.get(0));
        return null;
    }

    private String generateSmartNote(String query, List<MovieVO> movies, List<Long> categoryIds, List<String> regions) {
        if (movies.isEmpty()) return "没有找到相关影片，试试其他关键词吧。";

        String topTitle = movies.get(0).getTitle();
        StringBuilder note = new StringBuilder();

        if (query.contains("科幻")) note.append("科幻迷必看！");
        else if (query.contains("搞笑") || query.contains("喜剧")) note.append("笑到停不下来！");
        else if (query.contains("爱情") || query.contains("浪漫")) note.append("感动人心的爱情故事！");
        else if (query.contains("动作")) note.append("动作场面燃爆了！");
        else if (query.contains("动画")) note.append("画面精美绝伦！");
        else if (query.contains("悬疑") || query.contains("推理")) note.append("剧情反转再反转！");
        else note.append("为您精选以下影片：");

        note.append(" 推荐《").append(topTitle).append("》");

        if (!regions.isEmpty()) {
            String regionStr;
            if (query.contains("国外") || query.contains("外国")) {
                regionStr = "国外";
            } else {
                regionStr = String.join("/", regions.subList(0, Math.min(2, regions.size())));
            }
            note.append("，").append(regionStr).append("佳作");
        }

        note.append("，评分").append(movies.get(0).getRating());

        if (movies.size() > 1) {
            note.append("。共找到").append(movies.size()).append("部相关影片：");
            for (int i = 1; i < Math.min(movies.size(), 5); i++) {
                note.append("《").append(movies.get(i).getTitle()).append("》");
                if (i < Math.min(movies.size(), 5) - 1) note.append("、");
            }
        }

        return note.toString();
    }

    @GetMapping("/recommend")
    public Result<List<MovieVO>> getRecommendations(@RequestParam(required = false) Long userId) {
        // 未登录或没有userId，返回热门
        if (userId == null) {
            return Result.success(movieService.getHotMovies(6));
        }
        List<Long> historyMovieIds = playRecordMapper.selectList(
                new QueryWrapper<PlayRecord>()
                        .eq("user_id", userId)
                        .select("DISTINCT movie_id")
        ).stream().map(PlayRecord::getMovieId).collect(Collectors.toList());

        if (historyMovieIds.isEmpty()) {
            return Result.success(movieService.getHotMovies(6));
        }

        Set<Long> watchedSet = new HashSet<>(historyMovieIds);

        // 统计用户看过的分类频率
        Map<Long, Integer> catFreq = new HashMap<>();
        Map<String, Integer> regionFreq = new HashMap<>();
        for (Long mid : historyMovieIds) {
            Movie m = movieMapper.selectById(mid);
            if (m != null && m.getRegion() != null) {
                regionFreq.put(m.getRegion(), regionFreq.getOrDefault(m.getRegion(), 0) + 1);
            }
            movieTypeMapper.selectList(new QueryWrapper<MovieType>().eq("movie_id", mid))
                .forEach(t -> catFreq.put(t.getCategoryId(), catFreq.getOrDefault(t.getCategoryId(), 0) + 1));
        }

        // 按分类频率降序排列
        List<Long> sortedCats = catFreq.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 按地区频率排序
        List<String> sortedRegions = regionFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Set<Long> recommendIds = new LinkedHashSet<>();

        // 优先从频率最高的分类中推荐
        for (Long cid : sortedCats) {
            movieTypeMapper.selectList(new QueryWrapper<MovieType>().eq("category_id", cid))
                .stream().map(MovieType::getMovieId)
                .filter(id -> !watchedSet.contains(id))
                .forEach(id -> {
                    if (recommendIds.size() < 6) recommendIds.add(id);
                });
        }

        // 不够再按地区推荐
        if (recommendIds.size() < 6) {
            for (String reg : sortedRegions) {
                movieMapper.selectList(new QueryWrapper<Movie>().eq("region", reg).eq("status", 1).orderByDesc("play_count"))
                    .stream().map(Movie::getId)
                    .filter(id -> !watchedSet.contains(id))
                    .forEach(id -> {
                        if (recommendIds.size() < 6) recommendIds.add(id);
                    });
            }
        }

        // 还不够补热门
        if (recommendIds.size() < 6) {
            movieMapper.selectList(new QueryWrapper<Movie>().eq("status", 1).orderByDesc("play_count").last("LIMIT 20"))
                .forEach(m -> {
                    if (!watchedSet.contains(m.getId()) && recommendIds.size() < 6) recommendIds.add(m.getId());
                });
        }

        List<MovieVO> result = recommendIds.stream()
                .map(movieService::getMovieDetail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Result.success(result);
    }
}

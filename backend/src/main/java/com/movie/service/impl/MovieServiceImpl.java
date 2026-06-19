package com.movie.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.dto.CrewVO;
import com.movie.dto.MovieVO;
import com.movie.entity.*;
import com.movie.mapper.*;
import com.movie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private MovieTypeMapper movieTypeMapper;

    @Autowired
    private CrewMapper crewMapper;

    @Autowired
    private MovieCrewMapper movieCrewMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<MovieVO> getHotMovies(int limit) {
        // 先查Redis缓存
        String cacheKey = "movie:hot";
        @SuppressWarnings("unchecked")
        List<MovieVO> cached = (List<MovieVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查DB：按播放量降序
        List<Movie> movies = movieMapper.selectList(
                new QueryWrapper<Movie>()
                        .eq("status", 1)
                        .orderByDesc("play_count")
                        .last("LIMIT " + limit)
        );

        List<MovieVO> voList = movies.stream().map(this::convertToVO).collect(Collectors.toList());

        // 写入Redis缓存（1小时过期）
        redisTemplate.opsForValue().set(cacheKey, voList, 1, TimeUnit.HOURS);

        return voList;
    }

    @Override
    public Page<MovieVO> getMoviesByCategory(Long categoryId, int page, int size) {
        // 查询分类是否存在
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return new Page<>(page, size);
        }

        // 查询该分类下的电影ID
        List<Long> movieIds = movieTypeMapper.selectList(
                new QueryWrapper<MovieType>().eq("category_id", categoryId)
        ).stream().map(MovieType::getMovieId).collect(Collectors.toList());

        if (movieIds.isEmpty()) {
            return new Page<>(page, size);
        }

        // 分页查询电影
        Page<Movie> moviePage = movieMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<Movie>()
                        .in("id", movieIds)
                        .eq("status", 1)
                        .orderByDesc("play_count")
        );

        Page<MovieVO> voPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        voPage.setRecords(moviePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<MovieVO> getMoviesByRegion(String region, int page, int size) {
        Page<Movie> moviePage = movieMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<Movie>()
                        .eq("region", region)
                        .eq("status", 1)
                        .orderByDesc("play_count")
        );

        Page<MovieVO> voPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        voPage.setRecords(moviePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public MovieVO getMovieDetail(Long id) {
        // 先查Redis
        String cacheKey = "movie:detail:" + id;
        MovieVO cached = (MovieVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            return null;
        }

        MovieVO vo = convertToVO(movie);

        // 缓存30分钟
        redisTemplate.opsForValue().set(cacheKey, vo, 30, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(
                new QueryWrapper<Category>().orderByAsc("sort_order")
        );
    }

    @Override
    public List<String> getAllRegions() {
        return movieMapper.selectAllRegions();
    }

    @Override
    public Page<MovieVO> getMoviesByCategoryAndRegion(Long categoryId, String region, int page, int size) {
        List<Long> idsByCategory = null;
        List<Long> idsByRegion = null;

        if (categoryId != null) {
            idsByCategory = movieTypeMapper.selectList(
                    new QueryWrapper<MovieType>().eq("category_id", categoryId)
            ).stream().map(MovieType::getMovieId).collect(Collectors.toList());
            if (idsByCategory.isEmpty()) return new Page<>(page, size);
        }

        if (region != null && !region.isEmpty()) {
            idsByRegion = movieMapper.selectList(
                    new QueryWrapper<Movie>().select("id").eq("region", region).eq("status", 1)
            ).stream().map(Movie::getId).collect(Collectors.toList());
            if (idsByRegion.isEmpty()) return new Page<>(page, size);
        }

        List<Long> finalIds = null;
        if (idsByCategory != null && idsByRegion != null) {
            idsByCategory.retainAll(idsByRegion);
            finalIds = idsByCategory.isEmpty() ? null : idsByCategory;
        } else if (idsByCategory != null) {
            finalIds = idsByCategory.isEmpty() ? null : idsByCategory;
        } else if (idsByRegion != null) {
            finalIds = idsByRegion.isEmpty() ? null : idsByRegion;
        }
        // 如果 categoryId 和 region 都为 null（即选择全部分类+全部地区），finalIds 为 null
        // 此时 finalIds==null 表示不加筛选条件，查询全部电影

        Page<Movie> moviePage;
        if (finalIds != null && !finalIds.isEmpty()) {
            moviePage = movieMapper.selectPage(new Page<>(page, size),
                    new QueryWrapper<Movie>().in("id", finalIds).eq("status", 1).orderByDesc("play_count"));
        } else if (finalIds == null) {
            // 无筛选条件（全部分类 + 全部地区），查全部
            moviePage = movieMapper.selectPage(new Page<>(page, size),
                    new QueryWrapper<Movie>().eq("status", 1).orderByDesc("play_count"));
        } else {
            return new Page<>(page, size);
        }

        Page<MovieVO> voPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        voPage.setRecords(moviePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<MovieVO> searchMovies(String keyword, int page, int size) {
        Page<Movie> moviePage = movieMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<Movie>()
                        .like("title", keyword)
                        .eq("status", 1)
                        .orderByDesc("play_count")
        );

        Page<MovieVO> voPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        voPage.setRecords(moviePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<MovieVO> getLatestMovies(int limit) {
        List<Movie> movies = movieMapper.selectList(
                new QueryWrapper<Movie>()
                        .eq("status", 1)
                        .orderByDesc("release_date")
                        .last("LIMIT " + limit)
        );
        return movies.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public MovieVO convertToVO(Movie movie) {
        if (movie == null) return null;

        MovieVO vo = new MovieVO();
        BeanUtil.copyProperties(movie, vo);

        // 查询分类名称
        List<Long> categoryIds = movieMapper.selectCategoryIdsByMovieId(movie.getId());
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            vo.setCategoryNames(categories.stream().map(Category::getName).collect(Collectors.toList()));
        }

        // 查询主创信息
        List<MovieCrew> movieCrews = movieCrewMapper.selectList(
                new QueryWrapper<MovieCrew>().eq("movie_id", movie.getId()).orderByAsc("sort_order")
        );

        List<CrewVO> directors = new ArrayList<>();
        List<CrewVO> actors = new ArrayList<>();

        for (MovieCrew mc : movieCrews) {
            Crew crew = crewMapper.selectById(mc.getCrewId());
            if (crew == null) continue;

            CrewVO crewVO = new CrewVO();
            crewVO.setId(crew.getId());
            crewVO.setName(crew.getName());
            crewVO.setAvatar(crew.getAvatar());
            crewVO.setRoleType(crew.getRoleType());
            crewVO.setCharacterName(mc.getCharacterName());
            crewVO.setPosition(mc.getPosition());

            // 用 movie_crew.position 判断：一个人可同时是导演和演员
            if ("导演".equals(mc.getPosition())) {
                directors.add(crewVO);
            } else {
                actors.add(crewVO);
            }
        }

        vo.setDirectors(directors);
        vo.setActors(actors);

        return vo;
    }
}

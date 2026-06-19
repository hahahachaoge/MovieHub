package com.movie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.dto.MovieVO;
import com.movie.entity.Category;
import com.movie.entity.Movie;

import java.util.List;

public interface MovieService {

    /**
     * 获取热播电影列表
     */
    List<MovieVO> getHotMovies(int limit);

    /**
     * 按分类获取电影（分页）
     */
    Page<MovieVO> getMoviesByCategory(Long categoryId, int page, int size);

    /**
     * 按地区获取电影（分页）
     */
    Page<MovieVO> getMoviesByRegion(String region, int page, int size);

    /**
     * 获取电影详情
     */
    MovieVO getMovieDetail(Long id);

    /**
     * 获取所有分类
     */
    List<Category> getAllCategories();

    /**
     * 获取所有地区
     */
    List<String> getAllRegions();

    /**
     * 按分类+地区组合查询（分页）
     */
    Page<MovieVO> getMoviesByCategoryAndRegion(Long categoryId, String region, int page, int size);

    /**
     * 搜索电影
     */
    Page<MovieVO> searchMovies(String keyword, int page, int size);

    /**
     * 获取最新电影
     */
    List<MovieVO> getLatestMovies(int limit);

    /**
     * 实体转VO
     */
    MovieVO convertToVO(Movie movie);
}

package com.movie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.entity.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MovieMapper extends BaseMapper<Movie> {

    /**
     * 查询电影关联的分类ID列表
     */
    @Select("SELECT category_id FROM movie_type WHERE movie_id = #{movieId}")
    List<Long> selectCategoryIdsByMovieId(@Param("movieId") Long movieId);

    /**
     * 按分类ID查询电影ID列表
     */
    @Select("SELECT movie_id FROM movie_type WHERE category_id = #{categoryId}")
    List<Long> selectMovieIdsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 获取所有不重复的地区列表
     */
    @Select("SELECT DISTINCT region FROM movie WHERE region IS NOT NULL AND region != '' AND status = 1 ORDER BY region")
    List<String> selectAllRegions();

    /**
     * 按地区统计电影数量
     */
    @Select("SELECT region, COUNT(*) AS count FROM movie WHERE status = 1 AND region IS NOT NULL AND region != '' GROUP BY region ORDER BY count DESC")
    List<Map<String, Object>> selectCountByRegion();

    /**
     * 按关键字搜索电影标题
     */
    @Select("SELECT * FROM movie WHERE title LIKE CONCAT('%', #{keyword}, '%') AND status = 1")
    List<Movie> searchByKeyword(@Param("keyword") String keyword);
}

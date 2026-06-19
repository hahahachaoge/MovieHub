package com.movie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.BusinessException;
import com.movie.dto.MovieVO;
import com.movie.entity.Crew;
import com.movie.entity.Movie;
import com.movie.entity.MovieCrew;
import com.movie.mapper.CrewMapper;
import com.movie.mapper.MovieCrewMapper;
import com.movie.mapper.MovieMapper;
import com.movie.service.CrewService;
import com.movie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.movie.entity.Crew;
import com.movie.entity.MovieCrew;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrewServiceImpl implements CrewService {

    @Autowired
    private CrewMapper crewMapper;

    @Autowired
    private MovieCrewMapper movieCrewMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private MovieService movieService;

    @Override
    public Page<Crew> searchCrew(String keyword, int page, int size) {
        Page<Crew> crewPage = crewMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<Crew>()
                        .like("name", keyword)
                        .orderByAsc("role_type", "name")
        );
        // 计算 displayRole：判断是否同时是导演和演员
        for (Crew c : crewPage.getRecords()) {
            boolean isActor = movieCrewMapper.selectCount(
                    new QueryWrapper<MovieCrew>().eq("crew_id", c.getId()).eq("position", "演员")) > 0;
            boolean isDirector = movieCrewMapper.selectCount(
                    new QueryWrapper<MovieCrew>().eq("crew_id", c.getId()).eq("position", "导演")) > 0;
            if (isActor && isDirector) {
                c.setDisplayRole("BOTH");
            } else if (isDirector) {
                c.setDisplayRole("DIRECTOR");
            } else {
                c.setDisplayRole("ACTOR");
            }
        }
        return crewPage;
    }

    @Override
    public Crew getCrewDetail(Long crewId) {
        Crew crew = crewMapper.selectById(crewId);
        if (crew == null) {
            throw new BusinessException(404, "主创人员不存在");
        }
        return crew;
    }

    @Override
    public Page<MovieVO> getMoviesByCrewId(Long crewId, int page, int size) {
        // 确认主创存在
        getCrewDetail(crewId);

        // 查询关联的电影ID
        List<Long> movieIds = movieCrewMapper.selectList(
                new QueryWrapper<MovieCrew>().eq("crew_id", crewId)
        ).stream().map(MovieCrew::getMovieId).collect(Collectors.toList());

        if (movieIds.isEmpty()) {
            return new Page<>(page, size);
        }

        // 分页查询电影
        Page<Movie> moviePage = movieMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<Movie>()
                        .in("id", movieIds)
                        .eq("status", 1)
                        .orderByDesc("release_date")
        );

        Page<MovieVO> voPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        voPage.setRecords(moviePage.getRecords().stream().map(movieService::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Map<String, Object> getMoviesByCrewIdWithRoles(Long crewId) {
        getCrewDetail(crewId);

        // 查导演的电影
        List<Long> directorMovieIds = movieCrewMapper.selectList(
                new QueryWrapper<MovieCrew>().eq("crew_id", crewId).eq("position", "导演")
        ).stream().map(MovieCrew::getMovieId).collect(Collectors.toList());

        // 查演员的电影
        List<Long> actorMovieIds = movieCrewMapper.selectList(
                new QueryWrapper<MovieCrew>().eq("crew_id", crewId).eq("position", "演员")
        ).stream().map(MovieCrew::getMovieId).collect(Collectors.toList());

        List<MovieVO> directorMovies = new ArrayList<>();
        List<MovieVO> actorMovies = new ArrayList<>();

        if (!directorMovieIds.isEmpty()) {
            directorMovies = movieMapper.selectList(
                    new QueryWrapper<Movie>().in("id", directorMovieIds).eq("status", 1).orderByDesc("release_date")
            ).stream().map(movieService::convertToVO).collect(Collectors.toList());
        }

        if (!actorMovieIds.isEmpty()) {
            actorMovies = movieMapper.selectList(
                    new QueryWrapper<Movie>().in("id", actorMovieIds).eq("status", 1).orderByDesc("release_date")
            ).stream().map(movieService::convertToVO).collect(Collectors.toList());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("directorMovies", directorMovies);
        result.put("actorMovies", actorMovies);
        return result;
    }
}

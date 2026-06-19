package com.movie.controller;

import com.movie.common.Result;
import com.movie.dto.MovieVO;
import com.movie.entity.Crew;
import com.movie.service.CrewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crew")
public class CrewController {

    @Autowired
    private CrewService crewService;

    @GetMapping("/search")
    public Result<Map<String, Object>> searchCrew(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageResult = crewService.searchCrew(keyword, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}/detail")
    public Result<Crew> getCrewDetail(@PathVariable Long id) {
        return Result.success(crewService.getCrewDetail(id));
    }

    @GetMapping("/{id}/movies")
    public Result<Map<String, Object>> getMoviesByCrew(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        var pageResult = crewService.getMoviesByCrewId(id, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return Result.success(data);
    }

    /**
     * 获取主创作品（按角色分类：导演作品 / 参演电影）
     */
    @GetMapping("/{id}/movies-by-role")
    public Result<Map<String, Object>> getMoviesByRole(@PathVariable Long id) {
        return Result.success(crewService.getMoviesByCrewIdWithRoles(id));
    }
}

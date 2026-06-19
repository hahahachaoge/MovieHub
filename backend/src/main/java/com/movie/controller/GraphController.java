package com.movie.controller;

import com.movie.common.Result;
import com.movie.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    @Autowired
    private GraphService graphService;

    /**
     * 获取热门电影关系图（默认TOP20）
     */
    @GetMapping("/hot")
    public Result<Map<String, Object>> getHotGraph(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(graphService.getHotGraph(limit));
    }

    /**
     * 搜索电影或演员的关系图
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> searchGraph(@RequestParam String keyword,
                                                    @RequestParam(defaultValue = "30") int limit) {
        return Result.success(graphService.searchGraph(keyword, limit));
    }
}

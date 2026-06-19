package com.movie.controller;

import com.movie.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("application", "电影网站 Movie Website");
        info.put("version", "1.0.0");
        info.put("timestamp", java.time.LocalDateTime.now().toString());
        return Result.success(info);
    }
}

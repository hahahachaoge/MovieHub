package com.movie.service;

import java.util.Map;

public interface GraphService {
    Map<String, Object> getHotGraph(int limit);
    Map<String, Object> searchGraph(String keyword, int limit);
}

package com.movie.service.impl;

import com.movie.service.GraphService;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphServiceImpl implements GraphService {

    @Autowired
    private Driver neo4jDriver;

    @Override
    public Map<String, Object> getHotGraph(int limit) {
        String cypher = """
            MATCH (m:Movie)
            OPTIONAL MATCH (p:Person)-[:ACTED_IN]->(m)
            OPTIONAL MATCH (p2:Person)-[:DIRECTED]->(m)
            OPTIONAL MATCH (m)-[:BELONGS_TO]->(c:Category)
            WITH m, collect(DISTINCT p) AS actors, collect(DISTINCT p2) AS directors, collect(DISTINCT c) AS categories
            LIMIT $limit
            UNWIND CASE WHEN size(actors)=0 THEN [null] ELSE actors END AS actor
            UNWIND CASE WHEN size(directors)=0 THEN [null] ELSE directors END AS director
            UNWIND CASE WHEN size(categories)=0 THEN [null] ELSE categories END AS category
            RETURN m, actor, director, category
        """;
        return queryGraph(cypher, Map.of("limit", limit));
    }

    @Override
    public Map<String, Object> searchGraph(String keyword, int limit) {
        String cypher = """
            MATCH (m:Movie)
            WHERE m.title CONTAINS $keyword
            OPTIONAL MATCH (p:Person)-[:ACTED_IN]->(m)
            OPTIONAL MATCH (p2:Person)-[:DIRECTED]->(m)
            OPTIONAL MATCH (m)-[:BELONGS_TO]->(c:Category)
            WITH m, collect(DISTINCT p) AS actors, collect(DISTINCT p2) AS directors, collect(DISTINCT c) AS categories
            LIMIT $limit
            UNWIND CASE WHEN size(actors)=0 THEN [null] ELSE actors END AS actor
            UNWIND CASE WHEN size(directors)=0 THEN [null] ELSE directors END AS director
            UNWIND CASE WHEN size(categories)=0 THEN [null] ELSE categories END AS category
            RETURN m, actor, director, category
            UNION
            MATCH (p:Person)
            WHERE p.name CONTAINS $keyword
            OPTIONAL MATCH (p)-[:ACTED_IN]->(ma:Movie)
            OPTIONAL MATCH (p)-[:DIRECTED]->(md:Movie)
            WITH collect(DISTINCT ma) + collect(DISTINCT md) AS allMovies
            UNWIND allMovies AS m
            WITH m WHERE m IS NOT NULL
            OPTIONAL MATCH (p2:Person)-[:ACTED_IN]->(m)
            OPTIONAL MATCH (p3:Person)-[:DIRECTED]->(m)
            OPTIONAL MATCH (m)-[:BELONGS_TO]->(c:Category)
            WITH m, collect(DISTINCT p2) AS actors, collect(DISTINCT p3) AS directors, collect(DISTINCT c) AS categories
            LIMIT $limit
            UNWIND CASE WHEN size(actors)=0 THEN [null] ELSE actors END AS actor
            UNWIND CASE WHEN size(directors)=0 THEN [null] ELSE directors END AS director
            UNWIND CASE WHEN size(categories)=0 THEN [null] ELSE categories END AS category
            RETURN m, actor, director, category
        """;
        return queryGraph(cypher, Map.of("keyword", keyword, "limit", limit));
    }

    private Map<String, Object> queryGraph(String cypher, Map<String, Object> params) {
        Set<String> nodeIds = new HashSet<>();
        Set<String> edgeSet = new HashSet<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, params);
            while (result.hasNext()) {
                var record = result.next();
                var mNode = record.get("m");
                if (mNode == null || mNode.isNull()) continue;
                var movie = mNode.asNode();
                long mid = movie.get("id").asLong();
                addNode(nodes, nodeIds, "movie-" + mid, movie.get("title").asString(), "movie", "#e50914");

                // 演员
                var aNode = record.get("actor");
                if (aNode != null && !aNode.isNull()) {
                    var p = aNode.asNode();
                    long pid = p.get("id").asLong();
                    String t = p.get("type").asString();
                    String g = "both".equals(t) ? "both" : "actor";
                    String c = "both".equals(t) ? "#8b5cf6" : "#3498db";
                    addNode(nodes, nodeIds, "p-" + pid, p.get("name").asString(), g, c);
                    addEdge(edges, edgeSet, "p-" + pid, "movie-" + mid, "ACTED_IN", "#3498db");
                }

                // 导演
                var dNode = record.get("director");
                if (dNode != null && !dNode.isNull()) {
                    var p = dNode.asNode();
                    long pid = p.get("id").asLong();
                    String t = p.get("type").asString();
                    String g = "both".equals(t) ? "both" : "director";
                    String c = "both".equals(t) ? "#8b5cf6" : "#2ecc71";
                    addNode(nodes, nodeIds, "p-" + pid, p.get("name").asString(), g, c);
                    addEdge(edges, edgeSet, "p-" + pid, "movie-" + mid, "DIRECTED", "#2ecc71");
                }

                // 分类
                var cNode = record.get("category");
                if (cNode != null && !cNode.isNull()) {
                    var cat = cNode.asNode();
                    addNode(nodes, nodeIds, "cat-" + cat.get("id").asLong(), cat.get("name").asString(), "category", "#d4a017");
                    addEdge(edges, edgeSet, "movie-" + mid, "cat-" + cat.get("id").asLong(), "", "#d4a017");
                }
            }
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("nodes", nodes);
        r.put("edges", edges);
        return r;
    }

    private void addNode(List<Map<String, Object>> nodes, Set<String> ids,
                         String id, String label, String group, String color) {
        if (ids.contains(id)) return;
        ids.add(id);
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("id", id); n.put("label", label);
        n.put("group", group); n.put("color", color);
        nodes.add(n);
    }

    private void addEdge(List<Map<String, Object>> edges, Set<String> set,
                         String from, String to, String label, String color) {
        String k = from + "→" + to + ":" + label;
        if (set.contains(k)) return;
        set.add(k);
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("from", from); e.put("to", to);
        e.put("label", label); e.put("color", color);
        edges.add(e);
    }
}

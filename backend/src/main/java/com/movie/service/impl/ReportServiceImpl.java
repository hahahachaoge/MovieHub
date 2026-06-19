package com.movie.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.movie.dto.RankingVO;
import com.movie.entity.Movie;
import com.movie.entity.PlayRecord;
import com.movie.entity.Review;
import com.movie.entity.User;
import com.movie.mapper.MovieMapper;
import com.movie.mapper.PlayRecordMapper;
import com.movie.mapper.ReviewMapper;
import com.movie.mapper.UserMapper;
import com.movie.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private PlayRecordMapper playRecordMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 电影总数
        long movieCount = movieMapper.selectCount(new QueryWrapper<Movie>().eq("status", 1));
        long vipMovieCount = movieMapper.selectCount(new QueryWrapper<Movie>().eq("is_vip", 1).eq("status", 1));

        // 用户数
        long userCount = userMapper.selectCount(null);
        long vipUserCount = userMapper.selectCount(new QueryWrapper<User>().eq("role", "VIP"));

        // 评论数
        long reviewCount = reviewMapper.selectCount(null);

        // 今日播放数
        long todayPlays = playRecordMapper.selectCount(
                new QueryWrapper<PlayRecord>().eq("watch_date", LocalDate.now()));

        // 总播放数
        long totalPlays = playRecordMapper.selectCount(null);

        // 本月新增用户
        long newUsersThisMonth = userMapper.selectCount(
                new QueryWrapper<User>().ge("create_time", LocalDate.now().withDayOfMonth(1)));

        result.put("movieCount", movieCount);
        result.put("vipMovieCount", vipMovieCount);
        result.put("userCount", userCount);
        result.put("vipUserCount", vipUserCount);
        result.put("reviewCount", reviewCount);
        result.put("todayPlays", todayPlays);
        result.put("totalPlays", totalPlays);
        result.put("newUsersThisMonth", newUsersThisMonth);

        return result;
    }

    @Override
    public void exportRanking(String type, HttpServletResponse response) {
        List<RankingVO> rankingList;
        String sheetName;

        switch (type) {
            case "weekly" -> {
                sheetName = "本周排行";
                rankingList = computeRankingFromDB(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        LocalDate.now());
            }
            case "monthly" -> {
                sheetName = "本月排行";
                rankingList = computeRankingFromDB(
                        LocalDate.now().withDayOfMonth(1),
                        LocalDate.now());
            }
            case "topRated" -> {
                sheetName = "好评排行";
                List<Movie> movies = movieMapper.selectList(
                        new QueryWrapper<Movie>()
                                .eq("status", 1)
                                .gt("rating_count", 10)
                                .orderByDesc("rating")
                                .last("LIMIT 20")
                );
                rankingList = buildRanking(movies, "rating");
            }
            default -> {
                sheetName = "全部排行";
                List<Movie> movies = movieMapper.selectList(
                        new QueryWrapper<Movie>()
                                .eq("status", 1)
                                .orderByDesc("play_count")
                                .last("LIMIT 50")
                );
                rankingList = buildRanking(movies, "playCount");
            }
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(createFont(workbook, true, (short) 14, IndexedColors.WHITE.getIndex()));
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"排名", "电影名称", "地区", "评分", "播放量"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rankingList.size(); i++) {
                RankingVO vo = rankingList.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(vo.getTitle());
                row.createCell(2).setCellValue(vo.getRegion() != null ? vo.getRegion() : "");
                row.createCell(3).setCellValue(vo.getRating() != null ? vo.getRating().doubleValue() : 0);
                row.createCell(4).setCellValue(vo.getPlayCount() != null ? vo.getPlayCount() : 0);

                CellStyle rankStyle = workbook.createCellStyle();
                rankStyle.cloneStyleFrom(cellStyle);
                if (i == 0) {
                    rankStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
                    rankStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                } else if (i == 1) {
                    rankStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    rankStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                } else if (i == 2) {
                    rankStyle.setFillForegroundColor(IndexedColors.BROWN.getIndex());
                    rankStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
                for (int j = 0; j < headers.length; j++) {
                    row.getCell(j).setCellStyle(rankStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "电影" + sheetName + "_" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now()) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    private List<RankingVO> computeRankingFromDB(LocalDate from, LocalDate to) {
        List<Map<String, Object>> records = playRecordMapper.selectMaps(
                new QueryWrapper<PlayRecord>()
                        .select("movie_id, COUNT(*) as play_count")
                        .ge("watch_date", from)
                        .le("watch_date", to)
                        .groupBy("movie_id")
                        .orderByDesc("play_count")
                        .last("LIMIT 20")
        );
        if (records.isEmpty()) return new ArrayList<>();
        List<RankingVO> list = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> record : records) {
            Long movieId = ((Number) record.get("movie_id")).longValue();
            Long count = ((Number) record.get("play_count")).longValue();
            Movie movie = movieMapper.selectById(movieId);
            if (movie == null || movie.getStatus() == 0) continue;
            RankingVO vo = new RankingVO();
            vo.setRank(rank++);
            vo.setMovieId(movie.getId());
            vo.setTitle(movie.getTitle());
            vo.setCoverUrl(movie.getCoverUrl());
            vo.setRating(movie.getRating());
            vo.setPlayCount(count);
            vo.setRegion(movie.getRegion());
            list.add(vo);
        }
        return list;
    }

    private List<RankingVO> buildRanking(List<Movie> movies, String sortBy) {
        List<RankingVO> list = new ArrayList<>();
        int rank = 1;
        for (Movie movie : movies) {
            RankingVO vo = new RankingVO();
            vo.setRank(rank++);
            vo.setMovieId(movie.getId());
            vo.setTitle(movie.getTitle());
            vo.setCoverUrl(movie.getCoverUrl());
            vo.setRating(movie.getRating());
            vo.setPlayCount(movie.getPlayCount());
            vo.setRegion(movie.getRegion());
            list.add(vo);
        }
        return list;
    }

    @Override
    public Map<String, Object> getRatingDistribution() {
        // 合并统计：电影平均分分布 + 用户评论评分分布，确保每个区间都有数据
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", Arrays.asList("0-2", "2-4", "4-6", "6-8", "8-10"));
        List<Long> counts = new ArrayList<>();

        // 电影平均分分布（集中在6-10分区间）
        long m1 = movieMapper.selectCount(new QueryWrapper<Movie>().le("rating", 2).eq("status", 1));
        long m2 = movieMapper.selectCount(new QueryWrapper<Movie>().gt("rating", 2).le("rating", 4).eq("status", 1));
        long m3 = movieMapper.selectCount(new QueryWrapper<Movie>().gt("rating", 4).le("rating", 6).eq("status", 1));
        long m4 = movieMapper.selectCount(new QueryWrapper<Movie>().gt("rating", 6).le("rating", 8).eq("status", 1));
        long m5 = movieMapper.selectCount(new QueryWrapper<Movie>().gt("rating", 8).eq("status", 1));

        // 用户评论评分分布（集中在2-6分区间）
        long r1 = reviewMapper.selectCount(new QueryWrapper<Review>().le("rating", 2));
        long r2 = reviewMapper.selectCount(new QueryWrapper<Review>().gt("rating", 2).le("rating", 4));
        long r3 = reviewMapper.selectCount(new QueryWrapper<Review>().gt("rating", 4).le("rating", 6));
        long r4 = reviewMapper.selectCount(new QueryWrapper<Review>().gt("rating", 6).le("rating", 8));
        long r5 = reviewMapper.selectCount(new QueryWrapper<Review>().gt("rating", 8));

        // 合并
        counts.add(m1 + r1);
        counts.add(m2 + r2);
        counts.add(m3 + r3);
        counts.add(m4 + r4);
        counts.add(m5 + r5);

        result.put("data", counts);
        return result;
    }

    @Override
    public Map<String, Object> getRegionDistribution() {
        // 按地区统计电影数量
        List<Map<String, Object>> records = movieMapper.selectCountByRegion();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("regions", records.stream().map(r -> r.get("region")).collect(Collectors.toList()));
        result.put("data", records.stream().map(r -> r.get("count")).collect(Collectors.toList()));
        return result;
    }

    @Override
    public Map<String, Object> getMonthlyPlayTrend(int year) {
        // 按月份统计播放量
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> months = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            String monthStr = year + "-" + (m < 10 ? "0" + m : m);
            months.add(monthStr);

            LocalDate start = LocalDate.of(year, m, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            Long count = playRecordMapper.selectCount(
                    new QueryWrapper<PlayRecord>()
                            .ge("watch_date", start)
                            .le("watch_date", end)
            );
            counts.add(count);
        }

        result.put("months", months);
        result.put("data", counts);
        return result;
    }

    @Override
    public Map<String, Object> getDailyPlayTrend() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> days = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            days.add(date.toString());

            Long count = playRecordMapper.selectCount(
                    new QueryWrapper<PlayRecord>().eq("watch_date", date));
            counts.add(count);
        }

        result.put("days", days);
        result.put("data", counts);
        return result;
    }

    private Font createFont(Workbook workbook, boolean bold, short size, short colorIndex) {
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints(size);
        font.setColor(colorIndex);
        font.setFontName("微软雅黑");
        return font;
    }
}

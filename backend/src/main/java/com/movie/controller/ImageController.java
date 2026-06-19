package com.movie.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
public class ImageController {

    @RequestMapping("/tmdb_all_movie/**")
    public void serveFile(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 解析路径
            String uri = request.getRequestURI();
            String prefix = "/tmdb_all_movie/";
            String relativePath = uri.substring(uri.indexOf(prefix) + prefix.length());
            relativePath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);

            // 先查 backend/tmdb_all_movie/
            File file = new File("./tmdb_all_movie/" + relativePath);
            if (!file.exists()) {
                file = new File("../tmdb_all_movie/" + relativePath);
            }
            if (!file.exists()) {
                response.setStatus(404);
                return;
            }

            // 判断内容类型
            String name = relativePath.toLowerCase();
            String contentType;
            if (name.endsWith(".mp4")) contentType = "video/mp4";
            else if (name.endsWith(".png")) contentType = "image/png";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
            else contentType = "application/octet-stream";

            long fileLength = file.length();

            // 处理 Range 请求（视频播放必须支持）
            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileLength - 1;
                long contentLength = end - start + 1;

                response.setStatus(206);
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.setHeader("Accept-Ranges", "bytes");
                response.setContentLengthLong(contentLength);

                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    raf.seek(start);
                    byte[] buffer = new byte[8192];
                    long remaining = contentLength;
                    OutputStream os = response.getOutputStream();
                    while (remaining > 0) {
                        int read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (read == -1) break;
                        os.write(buffer, 0, read);
                        remaining -= read;
                    }
                    os.flush();
                }
            } else {
                // 非 Range 请求：直接返回全部
                response.setContentType(contentType);
                response.setHeader("Accept-Ranges", "bytes");
                response.setContentLengthLong(fileLength);

                try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                     OutputStream os = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.flush();
                }
            }
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}

package com.movie.controller;

import com.movie.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
public class UploadController {

    @PostMapping("/api/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                return Result.error("请选择文件");
            }
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".gif")) {
                return Result.error("仅支持 JPG/PNG/GIF 格式");
            }

            String baseDir = System.getProperty("user.dir");
            String dirPath = baseDir + File.separator + "tmdb_all_movie" + File.separator + "avatars" + File.separator;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dirPath = baseDir + File.separator + ".." + File.separator + "tmdb_all_movie" + File.separator + "avatars" + File.separator;
                dir = new File(dirPath);
            }
            if (!dir.exists()) {
                dir = new File(".." + File.separator + "tmdb_all_movie" + File.separator + "avatars" + File.separator);
            }
            dir.mkdirs();

            String fileName = UUID.randomUUID().toString() + ext;
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            String url = "/tmdb_all_movie/avatars/" + fileName;
            return Result.success(url);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}

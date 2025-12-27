package com.panyu.mybolg.controller;

import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.Upload;
import com.panyu.mybolg.service.UploadService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Resource
    private UploadService uploadService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String fileType,
                                            @RequestParam(required = false) Long userId) {
        Map<String, Object> data = uploadService.listFiles(pageNum, pageSize, fileType, userId);
        return Result.success(data);
    }

    /**
     * 上传文件（通用）
     */
    @PostMapping("/file")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String bizDir,
            @RequestParam(required = false) Long userId) {
        try {
            Map<String, Object> result = uploadService.uploadFile(file, bizDir, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传图片（验证图片类型）
     */
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "images") String bizDir,
            @RequestParam(required = false) Long userId) {
        try {
            Map<String, Object> result = uploadService.uploadImage(file, bizDir, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传文章封面
     */
    @PostMapping("/cover")
    public Result<Map<String, Object>> uploadCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long userId) {
        return uploadImage(file, "covers", userId);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long userId) {
        return uploadImage(file, "avatars", userId);
    }

    @PostMapping
    public Result<Upload> save(@RequestBody Upload upload) {
        uploadService.save(upload);
        return Result.success(upload);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = uploadService.deleteFile(id);
        if (!success) {
            return Result.error("删除失败");
        }
        return Result.success();
    }
}

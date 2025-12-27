package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.Upload;
import com.panyu.mybolg.mapper.UploadMapper;
import com.panyu.mybolg.service.UploadService;
import com.panyu.mybolg.utils.RustFsUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UploadServiceImpl extends ServiceImpl<UploadMapper, Upload> implements UploadService {
    
    @Resource
    private RustFsUtil rustFsUtil;
    
    @Override
    public Map<String, Object> listFiles(Integer pageNum, Integer pageSize, String fileType, Long userId) {
        Page<Upload> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Upload> wrapper = new LambdaQueryWrapper<>();
        
        if (fileType != null && !fileType.isEmpty()) {
            wrapper.like(Upload::getFileType, fileType);
        }
        if (userId != null) {
            wrapper.eq(Upload::getUserId, userId);
        }
        
        wrapper.orderByDesc(Upload::getCreateTime);
        Page<Upload> result = page(page, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        
        return data;
    }
    
    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String bizDir, Long userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        try {
            // 上传到 RustFS
            String key = rustFsUtil.upload(file, bizDir);
            String fileUrl = rustFsUtil.getFileUrl(key);
            
            // 保存记录到数据库
            Upload upload = new Upload();
            upload.setOriginalName(file.getOriginalFilename());
            upload.setFileName(key.substring(key.lastIndexOf('/') + 1));
            upload.setFilePath(key);
            upload.setFileUrl(fileUrl);
            upload.setFileType(file.getContentType());
            upload.setFileSize(file.getSize());
            upload.setUserId(userId);
            upload.setCreateTime(LocalDateTime.now());
            upload.setUpdateTime(LocalDateTime.now());
            
            save(upload);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", upload.getId());
            result.put("url", fileUrl);
            result.put("key", key);
            result.put("fileName", upload.getFileName());
            result.put("fileSize", upload.getFileSize());
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String bizDir, Long userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 验证是否为图片
        if (!rustFsUtil.isImageFile(file.getOriginalFilename())) {
            throw new RuntimeException("只支持图片文件（jpg, jpeg, png, gif, bmp, webp）");
        }
        
        return uploadFile(file, bizDir, userId);
    }
    
    @Override
    public boolean deleteFile(Long id) {
        // 查询文件信息
        Upload upload = getById(id);
        if (upload != null && upload.getFilePath() != null) {
            try {
                // 删除 RustFS 中的文件
                rustFsUtil.delete(upload.getFilePath());
            } catch (Exception e) {
                // 即使 RustFS 删除失败，也继续删除数据库记录
                System.err.println("删除 RustFS 文件失败: " + e.getMessage());
            }
        }
        
        return removeById(id);
    }
}

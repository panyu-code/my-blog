package com.panyu.mybolg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panyu.mybolg.entity.Upload;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UploadService extends IService<Upload> {
    
    /**
     * 分页查询文件列表
     */
    Map<String, Object> listFiles(Integer pageNum, Integer pageSize, String fileType, Long userId);
    
    /**
     * 上传文件（通用）
     */
    Map<String, Object> uploadFile(MultipartFile file, String bizDir, Long userId);
    
    /**
     * 上传图片（验证图片类型）
     */
    Map<String, Object> uploadImage(MultipartFile file, String bizDir, Long userId);
    
    /**
     * 删除文件（同时删除存储和数据库记录）
     */
    boolean deleteFile(Long id);
}

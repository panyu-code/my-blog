package com.panyu.mybolg.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @author: YuPan
 * @Desc:
 * @create: 2025-12-14 03:25
 **/
@Slf4j
@Component
public class RustFsUtil {

    @Value("${rustfs.endpoint}")
    private String endpoint;

    @Value("${rustfs.access-key}")
    private String accessKey;

    @Value("${rustfs.secret-key}")
    private String secretKey;

    @Value("${rustfs.bucket}")
    private String bucket;

    private S3Client s3Client;

    @PostConstruct
    public void init() {

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1) // RustFS 不校验
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .forcePathStyle(true) // 关键
                .build();

        log.info("RustFS S3Client initialized, endpoint={}", endpoint);
    }

    /**
     * 上传 MultipartFile
     */
    public String upload(MultipartFile file, String bizDir) {
        try {
            String key = buildKey(bizDir, file.getOriginalFilename());

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return key;
        } catch (IOException e) {
            throw new RuntimeException("RustFS upload failed", e);
        }
    }

    /**
     * 上传本地文件
     */
    public void upload(Path filePath, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.putObject(request, filePath);
    }

    /**
     * 下载文件
     */
    public void download(String key, Path targetPath) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.getObject(request, targetPath);
    }

    /**
     * 删除文件
     */
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    /**
     * 判断对象是否存在
     */
    public boolean exists(String key) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件访问 URL
     */
    public String getFileUrl(String key) {
        return endpoint + "/" + bucket + "/" + key;
    }

    /**
     * 批量删除文件
     */
    public void batchDelete(java.util.List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            try {
                delete(key);
            } catch (Exception e) {
                log.error("Delete file failed, key={}", key, e);
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * 验证文件类型（图片）
     */
    public boolean isImageFile(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")
                || ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }

    /**
     * 生成对象 key（防覆盖）
     */
    private String buildKey(String bizDir, String originalFilename) {
        return bizDir + "/"
                + LocalDate.now()
                + "/"
                + UUID.randomUUID()
                + "-"
                + originalFilename;
    }
}
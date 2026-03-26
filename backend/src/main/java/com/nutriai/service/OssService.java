package com.nutriai.service;

import com.nutriai.config.CosConfig;
import com.nutriai.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务 - 腾讯云COS对象存储（未配置时回退到本地存储）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    private final COSClient cosClient;
    private final CosConfig cosConfig;

    @Value("${nutriai.upload.max-size:10485760}")
    private Long maxFileSize; // 默认10MB

    @Value("${nutriai.upload.allowed-types:jpg,jpeg,png,gif}")
    private String allowedTypes;

    @Value("${nutriai.upload.local-path:./uploads}")
    private String localUploadPath;

    @Value("${nutriai.upload.base-url:http://localhost:8080}")
    private String uploadBaseUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/jpg"
    );

    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file) {
        validateFile(file);
        String extension = getFileExtension(file.getOriginalFilename());
        String key = "avatars/avatar_" + UUID.randomUUID() + "." + extension;
        return uploadToCos(file, key);
    }

    /**
     * 上传食物照片
     */
    public String uploadFoodPhoto(MultipartFile file) {
        validateFile(file);
        String extension = getFileExtension(file.getOriginalFilename());
        String key = "foods/food_" + UUID.randomUUID() + "." + extension;
        return uploadToCos(file, key);
    }

    /**
     * 判断是否配置了腾讯云COS
     */
    private boolean isCosConfigured() {
        return cosConfig.getSecretId() != null && !cosConfig.getSecretId().isBlank()
                && cosConfig.getSecretKey() != null && !cosConfig.getSecretKey().isBlank();
    }

    /**
     * 上传文件：COS可用时上传到COS，否则保存到本地
     */
    private String uploadToCos(MultipartFile file, String key) {
        if (!isCosConfigured()) {
            return saveToLocal(file, key);
        }
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(
                cosConfig.getBucket(), key, file.getInputStream(), metadata
            );
            cosClient.putObject(putRequest);

            String fileUrl = cosConfig.getCosBaseUrl() + "/" + key;
            log.info("文件上传COS成功: key={}, url={}, size={}", key, fileUrl, file.getSize());
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传COS失败，切换本地存储: key={}", key, e);
            return saveToLocal(file, key);
        }
    }

    /**
     * 保存文件到本地磁盘
     */
    private String saveToLocal(MultipartFile file, String key) {
        try {
            Path filePath = Paths.get(localUploadPath, key).toAbsolutePath();
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            String fileUrl = uploadBaseUrl + "/uploads/" + key;
            log.info("文件保存本地成功: path={}, url={}, size={}", filePath, fileUrl, file.getSize());
            return fileUrl;
        } catch (IOException e) {
            log.error("文件保存本地失败: key={}", key, e);
            throw BusinessException.User.fileUploadFailed();
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String cosBaseUrl = cosConfig.getCosBaseUrl();
            if (fileUrl.startsWith(cosBaseUrl)) {
                String key = fileUrl.substring(cosBaseUrl.length() + 1);
                cosClient.deleteObject(cosConfig.getBucket(), key);
                log.info("COS文件删除成功: key={}", key);
            } else {
                // 本地文件删除
                String localPrefix = uploadBaseUrl + "/uploads/";
                if (fileUrl.startsWith(localPrefix)) {
                    String key = fileUrl.substring(localPrefix.length());
                    Path filePath = Paths.get(localUploadPath, key).toAbsolutePath();
                    Files.deleteIfExists(filePath);
                    log.info("本地文件删除成功: path={}", filePath);
                }
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > maxFileSize) {
            throw BusinessException.User.fileSizeExceeded();
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw BusinessException.User.fileTypeNotAllowed();
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
        if (!allowedExtensions.contains(extension)) {
            throw BusinessException.User.fileTypeNotAllowed();
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}

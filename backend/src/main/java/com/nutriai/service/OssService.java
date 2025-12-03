package com.nutriai.service;

import com.nutriai.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * OSS文件上传服务
 * 当前使用本地存储模拟，生产环境应替换为阿里云OSS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {
    
    @Value("${nutriai.upload.max-size:10485760}")
    private Long maxFileSize; // 默认10MB
    
    @Value("${nutriai.upload.allowed-types:jpg,jpeg,png,gif}")
    private String allowedTypes;
    
    @Value("${nutriai.upload.local-path:uploads}")
    private String localUploadPath;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${nutriai.upload.base-url:http://localhost}")
    private String baseUrl;
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/jpg"
    );
    
    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file) {
        // 验证文件
        validateFile(file);
        
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = "avatar_" + UUID.randomUUID().toString() + "." + extension;
            
            // 创建上传目录
            Path uploadDir = Paths.get(localUploadPath, "avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());
            
            // 返回完整访问URL
            String fileUrl = baseUrl + ":" + serverPort + "/api/uploads/avatars/" + filename;
            
            log.info("头像上传成功: filename={}, url={}, size={}", filename, fileUrl, file.getSize());
            return fileUrl;
            
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw BusinessException.User.FILE_UPLOAD_FAILED;
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw BusinessException.User.FILE_SIZE_EXCEEDED;
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw BusinessException.User.FILE_TYPE_NOT_ALLOWED;
        }
        
        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException("文件名不能为空");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
        if (!allowedExtensions.contains(extension)) {
            throw BusinessException.User.FILE_TYPE_NOT_ALLOWED;
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
    
    /**
     * 上传食物照片
     */
    public String uploadFoodPhoto(MultipartFile file) {
        // 验证文件
        validateFile(file);
        
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = "food_" + UUID.randomUUID().toString() + "." + extension;
            
            // 创建上传目录
            Path uploadDir = Paths.get(localUploadPath, "foods");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());
            
            // 返回完整访问URL
            String fileUrl = baseUrl + ":" + serverPort + "/api/uploads/foods/" + filename;
            
            log.info("食物照片上传成功: filename={}, url={}, size={}", filename, fileUrl, file.getSize());
            return fileUrl;
            
        } catch (IOException e) {
            log.error("食物照片上传失败", e);
            throw BusinessException.User.FILE_UPLOAD_FAILED;
        }
    }
    
    /**
     * 删除文件（用于更换头像时删除旧头像）
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // 从URL提取文件路径
            String filePath = fileUrl.replace("/uploads/", "");
            Path path = Paths.get(localUploadPath, filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }
}

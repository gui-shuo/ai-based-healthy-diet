package com.nutriai.service;

import com.nutriai.dto.user.*;
import com.nutriai.entity.User;
import com.nutriai.exception.BusinessException;
import com.nutriai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    private static final int SMS_CODE_LENGTH = 6;
    
    /**
     * 获取用户资料
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.User.USER_NOT_FOUND);
        
        return UserProfileResponse.fromEntity(user);
    }
    
    /**
     * 更新用户资料
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.User.USER_NOT_FOUND);
        
        // 更新邮箱
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // 检查邮箱是否已被使用
            if (userRepository.existsByEmail(request.getEmail()) && 
                !request.getEmail().equals(user.getEmail())) {
                throw BusinessException.User.EMAIL_ALREADY_EXISTS;
            }
            user.setEmail(request.getEmail());
        }
        
        // 更新昵称
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        
        // 更新头像
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        user = userRepository.save(user);
        log.info("用户资料更新成功: userId={}", userId);
        
        return UserProfileResponse.fromEntity(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 验证两次密码是否一致
        if (!request.isPasswordMatch()) {
            throw BusinessException.Auth.PASSWORD_MISMATCH;
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.User.USER_NOT_FOUND);
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.User.OLD_PASSWORD_INCORRECT;
        }
        
        // 检查新密码不能与旧密码相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw BusinessException.User.NEW_PASSWORD_SAME_AS_OLD;
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("用户密码修改成功: userId={}", userId);
    }
    
    /**
     * 发送短信验证码
     */
    public SmsCodeResponse sendSmsCode(String phone) {
        // 检查手机号是否已被注册
        if (userRepository.existsByPhone(phone)) {
            throw BusinessException.User.PHONE_ALREADY_EXISTS;
        }
        
        // 生成6位数字验证码
        String code = generateSmsCode();
        String key = UUID.randomUUID().toString();
        
        // 存储到Redis
        String redisKey = SMS_CODE_PREFIX + key;
        redisTemplate.opsForValue().set(
                redisKey, 
                code, 
                SMS_CODE_EXPIRE_MINUTES, 
                TimeUnit.MINUTES
        );
        
        // TODO: 实际项目中需要调用第三方短信服务发送验证码
        // 这里仅模拟发送
        log.info("发送短信验证码: phone={}, code={}, key={}", phone, code, key);
        
        return SmsCodeResponse.builder()
                .smsKey(key)
                .expiresIn((long) SMS_CODE_EXPIRE_MINUTES * 60)
                .message("验证码已发送，5分钟内有效")
                .build();
    }
    
    /**
     * 修改手机号
     */
    @Transactional
    public void changePhone(Long userId, ChangePhoneRequest request) {
        // 验证短信验证码
        validateSmsCode(request.getSmsKey(), request.getSmsCode());
        
        // 检查新手机号是否已被使用
        if (userRepository.existsByPhone(request.getNewPhone())) {
            throw BusinessException.User.PHONE_ALREADY_EXISTS;
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.User.USER_NOT_FOUND);
        
        user.setPhone(request.getNewPhone());
        userRepository.save(user);
        
        // 删除已使用的验证码
        String redisKey = SMS_CODE_PREFIX + request.getSmsKey();
        redisTemplate.delete(redisKey);
        
        log.info("用户手机号修改成功: userId={}, newPhone={}", userId, request.getNewPhone());
    }
    
    /**
     * 验证短信验证码
     */
    private void validateSmsCode(String key, String code) {
        if (key == null || code == null) {
            throw BusinessException.User.SMS_CODE_INVALID;
        }
        
        String redisKey = SMS_CODE_PREFIX + key;
        String storedCode = (String) redisTemplate.opsForValue().get(redisKey);
        
        if (storedCode == null) {
            throw BusinessException.User.SMS_CODE_EXPIRED;
        }
        
        if (!storedCode.equalsIgnoreCase(code)) {
            throw BusinessException.User.SMS_CODE_INVALID;
        }
    }
    
    /**
     * 生成短信验证码
     */
    private String generateSmsCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < SMS_CODE_LENGTH; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }
}

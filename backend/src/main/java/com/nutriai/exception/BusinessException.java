package com.nutriai.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    private final String message;
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(String message) {
        this(400, message);
    }
    
    /**
     * 认证相关业务异常
     */
    public static class Auth {
        public static BusinessException USERNAME_EXISTS = new BusinessException(40001, "用户名已存在");
        public static BusinessException EMAIL_EXISTS = new BusinessException(40002, "邮箱已被注册");
        public static BusinessException PHONE_EXISTS = new BusinessException(40003, "手机号已被注册");
        public static BusinessException PASSWORD_MISMATCH = new BusinessException(40004, "两次密码输入不一致");
        public static BusinessException CAPTCHA_INVALID = new BusinessException(40005, "验证码错误或已过期");
        public static BusinessException LOGIN_FAILED = new BusinessException(40006, "用户名或密码错误");
        public static BusinessException ACCOUNT_DISABLED = new BusinessException(40007, "账号已被禁用");
        public static BusinessException ACCOUNT_BANNED = new BusinessException(40008, "账号已被封禁");
        public static BusinessException TOKEN_INVALID = new BusinessException(40009, "令牌无效或已过期");
        public static BusinessException USER_NOT_FOUND = new BusinessException(40010, "用户不存在");
        public static BusinessException CAPTCHA_REQUIRED = new BusinessException(40011, "登录失败次数过多，需要验证码");
    }
    
    /**
     * 用户相关业务异常
     */
    public static class User {
        public static BusinessException USER_NOT_FOUND = new BusinessException(40101, "用户不存在");
        public static BusinessException EMAIL_ALREADY_EXISTS = new BusinessException(40102, "邮箱已被使用");
        public static BusinessException PHONE_ALREADY_EXISTS = new BusinessException(40103, "手机号已被使用");
        public static BusinessException OLD_PASSWORD_INCORRECT = new BusinessException(40104, "旧密码不正确");
        public static BusinessException NEW_PASSWORD_SAME_AS_OLD = new BusinessException(40105, "新密码不能与旧密码相同");
        public static BusinessException SMS_CODE_INVALID = new BusinessException(40106, "短信验证码错误");
        public static BusinessException SMS_CODE_EXPIRED = new BusinessException(40107, "短信验证码已过期");
        public static BusinessException FILE_UPLOAD_FAILED = new BusinessException(40108, "文件上传失败");
        public static BusinessException FILE_TYPE_NOT_ALLOWED = new BusinessException(40109, "不支持的文件类型");
        public static BusinessException FILE_SIZE_EXCEEDED = new BusinessException(40110, "文件大小超过限制");
    }
}

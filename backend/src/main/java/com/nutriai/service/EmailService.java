package com.nutriai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务类 - 支持阿里云/腾讯云/标准SMTP邮件发送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@nutriai.com}")
    private String fromEmail;
    
    @Value("${spring.mail.nickname:NutriAI健康饮食助手}")
    private String nickname;
    
    /**
     * 发送手机号修改验证码
     */
    public void sendPhoneChangeCode(String toEmail, String username, String code) {
        String subject = "【NutriAI】手机号修改验证码";
        String content = buildPhoneChangeCodeHtml(username, code);
        sendHtmlMail(toEmail, subject, content);
    }

    /**
     * 发送注册邮箱验证码
     */
    public void sendRegisterCode(String toEmail, String code) {
        String subject = "【NutriAI】注册邮箱验证码";
        String content = buildRegisterCodeHtml(code);
        sendHtmlMail(toEmail, subject, content);
    }

    /**
     * 发送密码重置验证码
     */
    public void sendResetCode(String toEmail, String username, String code) {
        String subject = "【NutriAI】密码重置验证码";
        String content = buildResetCodeHtml(username, code);
        sendHtmlMail(toEmail, subject, content);
    }
    
    /**
     * 发送反馈通知邮件给管理员
     */
    @Async
    public void sendFeedbackNotification(String title, String type, String content,
                                          String username, String contactInfo) {
        String typeText = switch (type) {
            case "BUG" -> "🐛 Bug报告";
            case "FEATURE" -> "💡 功能建议";
            case "SUGGESTION" -> "📝 意见建议";
            default -> "📋 其他反馈";
        };

        String subject = "【NutriAI反馈】" + typeText + " - " + title;
        String htmlContent = buildFeedbackNotificationHtml(title, typeText, content, username, contactInfo);
        sendHtmlMail(fromEmail, subject, htmlContent);
    }

    /**
     * 构建反馈通知邮件HTML
     */
    private String buildFeedbackNotificationHtml(String title, String typeText, String content,
                                                   String username, String contactInfo) {
        String contact = (contactInfo != null && !contactInfo.isEmpty()) ? contactInfo : "未提供";
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background-color:#f4f4f4;font-family:'Microsoft YaHei',Arial,sans-serif;">
              <div style="max-width:600px;margin:40px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                <div style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:30px;text-align:center;">
                  <h1 style="color:#ffffff;margin:0;font-size:28px;">🥗 NutriAI</h1>
                  <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">用户反馈通知</p>
                </div>
                <div style="padding:32px;">
                  <div style="background:#f0f4ff;border-left:4px solid #667eea;padding:12px 16px;border-radius:0 8px 8px 0;margin-bottom:20px;">
                    <span style="color:#667eea;font-weight:bold;font-size:14px;">%s</span>
                  </div>
                  <h2 style="color:#1f2937;font-size:18px;margin:0 0 12px;">%s</h2>
                  <div style="background:#f9fafb;padding:16px;border-radius:8px;margin-bottom:20px;">
                    <p style="color:#4b5563;font-size:14px;line-height:1.8;margin:0;white-space:pre-wrap;">%s</p>
                  </div>
                  <table style="width:100%%;border-collapse:collapse;font-size:13px;color:#6b7280;">
                    <tr>
                      <td style="padding:6px 0;"><strong>提交用户：</strong>%s</td>
                    </tr>
                    <tr>
                      <td style="padding:6px 0;"><strong>联系方式：</strong>%s</td>
                    </tr>
                  </table>
                </div>
                <div style="background:#f8f9fa;padding:16px;text-align:center;border-top:1px solid #eee;">
                  <p style="color:#aaa;font-size:12px;margin:0;">请登录管理后台查看并处理此反馈</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(typeText, title, content, username, contact);
    }

    /**
     * 发送HTML邮件
     */
    private void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail, nickname);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    /**
     * 构建密码重置验证码邮件HTML
     */
    private String buildResetCodeHtml(String username, String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background-color:#f4f4f4;font-family:'Microsoft YaHei',Arial,sans-serif;">
              <div style="max-width:600px;margin:40px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                <div style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:30px;text-align:center;">
                  <h1 style="color:#ffffff;margin:0;font-size:28px;">🥗 NutriAI</h1>
                  <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">AI健康饮食规划助手</p>
                </div>
                <div style="padding:32px;">
                  <p style="color:#333;font-size:16px;">您好，<strong>%s</strong>：</p>
                  <p style="color:#555;font-size:14px;line-height:1.8;">您正在进行密码重置操作，请使用以下验证码完成验证：</p>
                  <div style="text-align:center;margin:28px 0;">
                    <span style="display:inline-block;background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;font-size:32px;font-weight:bold;letter-spacing:8px;padding:16px 40px;border-radius:8px;">%s</span>
                  </div>
                  <p style="color:#999;font-size:13px;line-height:1.6;">
                    ⏰ 验证码有效期为 <strong>15分钟</strong>，请尽快使用。<br>
                    🔒 如果您没有进行此操作，请忽略此邮件，您的账号仍然安全。
                  </p>
                </div>
                <div style="background:#f8f9fa;padding:16px;text-align:center;border-top:1px solid #eee;">
                  <p style="color:#aaa;font-size:12px;margin:0;">此邮件由系统自动发送，请勿直接回复</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(username, code);
    }

    /**
     * 构建注册邮箱验证码邮件HTML
     */
    private String buildRegisterCodeHtml(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background-color:#f4f4f4;font-family:'Microsoft YaHei',Arial,sans-serif;">
              <div style="max-width:600px;margin:40px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                <div style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:30px;text-align:center;">
                  <h1 style="color:#ffffff;margin:0;font-size:28px;">🥗 NutriAI</h1>
                  <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">AI健康饮食规划助手</p>
                </div>
                <div style="padding:32px;">
                  <p style="color:#333;font-size:16px;">您好：</p>
                  <p style="color:#555;font-size:14px;line-height:1.8;">感谢您注册 NutriAI！请使用以下验证码完成邮箱验证：</p>
                  <div style="text-align:center;margin:28px 0;">
                    <span style="display:inline-block;background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;font-size:32px;font-weight:bold;letter-spacing:8px;padding:16px 40px;border-radius:8px;">%s</span>
                  </div>
                  <p style="color:#999;font-size:13px;line-height:1.6;">
                    ⏰ 验证码有效期为 <strong>5分钟</strong>，请尽快使用。<br>
                    🔒 如果您没有进行注册操作，请忽略此邮件。
                  </p>
                </div>
                <div style="background:#f8f9fa;padding:16px;text-align:center;border-top:1px solid #eee;">
                  <p style="color:#aaa;font-size:12px;margin:0;">此邮件由系统自动发送，请勿直接回复</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(code);
    }

    /**
     * 构建手机号修改验证码邮件HTML
     */
    private String buildPhoneChangeCodeHtml(String username, String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background-color:#f4f4f4;font-family:'Microsoft YaHei',Arial,sans-serif;">
              <div style="max-width:600px;margin:40px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                <div style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:30px;text-align:center;">
                  <h1 style="color:#ffffff;margin:0;font-size:28px;">🥗 NutriAI</h1>
                  <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">AI健康饮食规划助手</p>
                </div>
                <div style="padding:32px;">
                  <p style="color:#333;font-size:16px;">您好，<strong>%s</strong>：</p>
                  <p style="color:#555;font-size:14px;line-height:1.8;">您正在修改绑定的手机号，请使用以下验证码完成身份验证：</p>
                  <div style="text-align:center;margin:28px 0;">
                    <span style="display:inline-block;background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;font-size:32px;font-weight:bold;letter-spacing:8px;padding:16px 40px;border-radius:8px;">%s</span>
                  </div>
                  <p style="color:#999;font-size:13px;line-height:1.6;">
                    ⏰ 验证码有效期为 <strong>5分钟</strong>，请尽快使用。<br>
                    🔒 如果您没有发起此操作，请忽略此邮件并注意账号安全。
                  </p>
                </div>
                <div style="background:#f8f9fa;padding:16px;text-align:center;border-top:1px solid #eee;">
                  <p style="color:#aaa;font-size:12px;margin:0;">此邮件由系统自动发送，请勿直接回复</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(username, code);
    }
}

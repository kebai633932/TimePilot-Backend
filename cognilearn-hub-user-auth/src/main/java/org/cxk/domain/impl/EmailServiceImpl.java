package org.cxk.domain.impl;

import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.IEmailService;
import org.cxk.trigger.aop.EmailRateLimit;
import org.cxk.types.exception.BizException;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service

public class EmailServiceImpl implements IEmailService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 验证码有效期（分钟）
    //todo @Value注入
    private static final int CODE_EXPIRY_MINUTES = 10;
    // 最大重试次数
    //todo @Value注入
    private static final int MAX_RETRIES = 5;

    @Override
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, "TimePilot"));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);  // true 表示 HTML 内容

            javaMailSender.send(mimeMessage);
            log.info("邮件发送成功，收件人：{}", toEmail);

        } catch (Exception e) {
            log.error("邮件发送失败，收件人：{}，原因：{}", toEmail, e.getMessage());
            throw new BizException("邮件发送失败，请稍后重试");
        }
    }

    @Override
    public void sendVerificationEmail(String email) {
        // 1. 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 2. 设置Redis缓存（带随机过期时间防雪崩）
        String redisKey = getVerificationKey(email);
        long expirySeconds = TimeUnit.MINUTES.toSeconds(CODE_EXPIRY_MINUTES)
                + ThreadLocalRandom.current().nextInt(30); // 随机0-30秒

        try {
            // 直接设置Redis值，不使用超时控制
            RBucket<String> bucket = redissonClient.getBucket(redisKey);
            bucket.set(code,Duration.ofSeconds(expirySeconds)); // 推荐
            log.debug("验证码存储成功，邮箱：{}，有效期：{}秒", email, expirySeconds);

            // 3. 同步发送邮件（带指数退避重试）
//            CompletableFuture.runAsync(() -> );
            sendEmailWithRetry(email, code);
        } catch (Exception e) {
            log.error("验证码发送失败，邮箱：{}，原因：{}", email, e.getMessage());
            throw new BizException("系统繁忙，请稍后重试");
            //todo 需要等qps上来，Redis 宕机验证码系统就不可用了，需要降级、兜底等
        }
    }

    /**
     * 带重试机制的邮件发送，用 Lua 脚本做 Redis 分布式限流
     * @see org.cxk.trigger.aop.EmailRateLimitAspect
     * @see org.cxk.trigger.aop.EmailRateLimit
     */
    @Retryable(
            value = { Exception.class },              // 遇到哪些异常时触发重试
            maxAttempts = 4,                          // 最多尝试 4 次（初次 + 3 次重试）
            backoff = @Backoff(
                    delay = 1000,       // 初始延迟 1 秒
                    multiplier = 2,     // 每次重试的延迟乘以 2
                    maxDelay = 10000,   // 延迟最长不超过 10 秒
                    random = true       // 加入随机抖动（避免多个请求同时重试）
            )
    )
    @EmailRateLimit
    private void sendEmailWithRetry(String email, String code) {
        String subject = "TimePilot - 邮箱验证码";
        // 使用现代简洁风格的HTML模板
        String content = getModernEmailTemplate(code, CODE_EXPIRY_MINUTES);
        sendEmail(email, subject, content);
    }

    /**
     * 现代简洁风格的邮件模板（优化版本）
     */
    private String getModernEmailTemplate(String code, int expiryMinutes) {
        return String.format("""
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>邮箱验证码</title>
            <style>
                body { 
                    margin: 0; 
                    padding: 0; 
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                    background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .email-container {
                    background: white;
                    border-radius: 16px;
                    box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                    padding: 40px;
                    max-width: 400px;
                    width: 90%%;
                    text-align: center;
                }
                .logo {
                    font-size: 24px;
                    font-weight: bold;
                    color: #667eea;
                    margin-bottom: 20px;
                }
                .title {
                    font-size: 24px;
                    font-weight: 600;
                    color: #2d3748;
                    margin: 20px 0 10px;
                }
                .description {
                    color: #718096;
                    font-size: 16px;
                    margin-bottom: 30px;
                    line-height: 1.5;
                }
                .verification-code {
                    background: #f7fafc;
                    border: 2px dashed #667eea;
                    border-radius: 12px;
                    padding: 20px;
                    margin: 30px 0;
                    font-size: 32px;
                    font-weight: bold;
                    letter-spacing: 4px;
                    color: #667eea;
                    font-family: 'Monaco', 'Menlo', monospace;
                }
                .info-box {
                    background: #edf2f7;
                    border-radius: 8px;
                    padding: 15px;
                    margin: 20px 0;
                }
                .info-text {
                    color: #4a5568;
                    font-size: 14px;
                    margin: 0;
                }
                .warning-text {
                    color: #e53e3e;
                    font-size: 14px;
                    font-weight: 500;
                }
                .footer {
                    margin-top: 30px;
                    padding-top: 20px;
                    border-top: 1px solid #e2e8f0;
                    color: #a0aec0;
                    font-size: 12px;
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="logo">🚀 TimePilot</div>
                <h1 class="title">邮箱验证</h1>
                <p class="description">
                    您好！为了完成账户验证，请使用下面的验证码：
                </p>
                <div class="verification-code">%s</div>
                <div class="info-box">
                    <p class="info-text">⏰ 验证码有效期: %d 分钟</p>
                    <p class="warning-text">⚠️ 请勿将验证码告诉他人</p>
                </div>
                <div class="footer">
                    <p>此邮件由系统自动发送，请勿回复</p>
                    <p>© 2025 TimePilot. 保留所有权利</p>
                </div>
            </div>
        </body>
        </html>""", code, expiryMinutes);
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {
        String redisKey = getVerificationKey(email);

        try {
            RBucket<String> bucket = redissonClient.getBucket(redisKey);
            String storedCode = bucket.get();

            // 1. 验证码匹配
            if (code != null && code.equals(storedCode)) {
                bucket.delete(); // 验证成功后删除，防止重复使用验证码（重放攻击）
                return true;
            }
            //todo 需要等qps上来，Redis 宕机验证码系统就不可用了，需要降级、兜底等
            return false;

        } catch (Exception e) {
            log.error("验证码校验异常，邮箱：{}，原因：{}", email, e.getMessage());
            throw new BizException("验证服务暂不可用，请稍后重试");
        }
    }

    /**
     * 获取验证码存储Key
     */
    //todo redis的模板要统一在一起
    private String getVerificationKey(String email) {
        return "email:code:" + email;
    }
}
package org.cxk.service.impl;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.cxk.service.IEmailService;
import org.cxk.trigger.aop.EmailRateLimit;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import types.exception.BizException;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
    private static final int CODE_EXPIRY_MINUTES = 10;
    // 最大重试次数
    private static final int MAX_RETRIES = 5;

    @Override
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, "cognilearn-hub"));
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
            bucket.set(code, expirySeconds, TimeUnit.SECONDS);
            log.debug("验证码存储成功，邮箱：{}，有效期：{}秒", email, expirySeconds);

            // 3. 异步发送邮件（带指数退避重试）
            CompletableFuture.runAsync(() -> sendEmailWithRetry(email, code));

        } catch (Exception e) {
            log.error("Redis存储异常，邮箱：{}，原因：{}", email, e.getMessage());
            throw new BizException("系统繁忙，请稍后重试");
            //todo 需要等qps上来，Redis 宕机验证码系统就不可用了，需要降级、兜底等
        }
    }

    /**
     * 带重试机制的邮件发送， 用 Lua 脚本做 Redis 分布式限流
     * @see org.cxk.trigger.aop.EmailRateLimitAspect
     * @see org.cxk.trigger.aop.EmailRateLimit
     */
    @EmailRateLimit
    private void sendEmailWithRetry(String email, String code) {
        int attempt = 0;
        while (true) {
            try {
                String subject = "验证码通知";
                String content = String.format(
                        "您好，您的验证码是：<strong>%s</strong>，有效期为%d分钟，请及时验证。",
                        code, CODE_EXPIRY_MINUTES
                );

                sendEmail(email, subject, content);
                return; // 发送成功则退出

            } catch (Exception e) {
                attempt++;
                if (attempt > MAX_RETRIES) {
                    log.error("邮件最终发送失败，邮箱：{}", email, e);
                    break;
                }

                // 指数退避 + 随机抖动
                long delay = (long) (Math.pow(2, attempt) * 1000); // 2^attempt 毫秒
                delay += ThreadLocalRandom.current().nextInt(100, 500); // 随机抖动

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
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
    private String getVerificationKey(String email) {
        return "email:code:" + email;
    }
}
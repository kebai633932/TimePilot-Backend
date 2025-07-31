package org.cxk.service.impl;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.cxk.service.IEmailService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author KJH
 * @description 邮件验证码发送服务实现类
 * @create 2025/7/30 14:32
 */
@Slf4j
@Service
public class EmailServiceImpl implements IEmailService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}") // 从配置中读取发件人邮箱
    private String fromEmail;

    @Override
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            javaMailSender.send(message);
        } catch (Exception e) {
            System.out.println();
            log.error("邮件发送失败，收件人：{}，原因：{}", toEmail, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败");
        }
    }

    @Override
    public void sendVerificationEmail(String email) {
        // 1. 生成 6 位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 2. 构建 Redis key 并存入（5分钟有效）
        String redisKey = "email:code:" + email;
        redissonClient.getBucket(redisKey).set(code, 5, TimeUnit.MINUTES);

        // 3. 构建邮件内容
        String subject = "验证码通知";
        String content = "您好，您的验证码是：" + code + "，有效期为5分钟，请及时验证。";

        // 4. 发送邮件
        sendEmail(email, subject, content);
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {
        String redisKey = "email:code:" + email;
        String storedCode = (String) redissonClient.getBucket(redisKey).get();
        return code != null && code.equals(storedCode);
    }
}

package org.cxk.trigger.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author KJH
 * @description 邮箱验证码限流
 * @create 2025/8/2 15:56
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailRateLimit {
    /**
     * 限流key前缀   OTP：One-Time Password, 一次性密码，常用于短信验证码、邮件验证码等。
     */
    String key() default "otp:rate:limit:";
    // 邮箱维度
    int emailInterval() default 30; // 相同邮箱发送间隔(秒)
    int emailHourlyLimit() default 30; // 相同邮箱每小时最大次数

    // 设备ID维度
    int deviceInterval() default 30; // 相同设备发送间隔(秒)
    int deviceHourlyLimit() default 30; // 相同设备每小时最大次数
    // 手机号维度

    // 系统维度
    int dailyTotalLimit() default 500; // 每日系统总量限制

    // 黑名单
    int blacklistTtl() default 86400; // 24小时
}
